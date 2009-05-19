package net.beadsproject.beads.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.beadsproject.beads.core.AudioUtils;

/**
 * A Buffered Sample provides random access to an audio file. 
 * 
 * Only supports 16-bit samples at the moment.
 * TODO: Fix MP3 region boundary issues..
 * 
 * Notes:
 * - BufferedSample is not particularly thread-safe. 
 *   This shouldn't matter because SamplePlayers are not executed in parallel 
 *   when used in a UGen chain.
 *    
 * @author ben
 */
public class Sample implements Runnable {

	public enum BufferingRegime {
		TIMED,
		TOTAL
	};
	
	private BufferingRegime bufferingRegime;
	
	/** Store the sample data with native bit-depth. 
	 * Storing in native bit-depth reduces memory load, but increases cpu-load. 
	 */ 
	//public boolean storeInNativeBitDepth = true;	
	
	private boolean verbose;
	
	private AudioFile audioFile;

	/** The audio format. */
	private AudioFormat audioFormat;

	/** The number of channels. */
	private int nChannels;

	/** The number of sample frames. */
	private long nFrames;

	/** The length in milliseconds. */
	private float length;	
	
	private boolean isBigEndian;

	// regionSize is the number of frames per region
	// TODO: atm it is a static parameter - waiting to be tuned
	// TODO: does it make sense that regionSize = AudioContext.bufferSize is a suitable size?
	// these parameters are only used in the TIMED regime
	private int regionSize;    
	private int regionSizeInBytes; // the number of bytes per region (regionSize * nChannels * bitconversionfactor)
	
	private int lookahead = 1; // num region lookahead
	private int lookback = 0; // num regions lookback
	private int maxRegionsLoadedAtOnce; // when memory conservation is more important than run-time performance
	private long memory = 0;
	
	// time-based versions of the above variables, 
	// these ones get transformed into the above ones when samplerate/etc is known..
	private int t_lookahead = 100; // time lookahead, ms
	private int t_lookback = 0; 
	private long t_memory = 10000;   // age (ms) at which regions get removed (once they haven't been touched)
	private int t_bufferMax = 0; // max space we can use (in MB) (0 == unlimited)
	private float t_regionSize = 10000; // by default 10s
	
	// REGION DATA	
	private int numberOfRegions; // total number of regions
	private int numberOfRegionsLoaded; // number of loaded regions
	private byte[][] regions; // the actual data
	
	private long[] regionAge; // the age of each region (in ms)
	private long timeAtLastAgeUpdate; // the time at the last age updated operation
	
	private boolean[] regionQueued; // true if a region is currently queued
	private ConcurrentLinkedQueue<Integer> regionQueue; // a queue of regions to be loaded
	private Thread regionThread; // the thread that loads regions in the background
	private Lock[] regionLocks; // to support safe deletion/writing of regions
	
	// this parameter is only used if regime==TOTAL
	private byte[] sampleData;
	

	/**
     * Instantiates a new empty buffer with the specified audio format and
     * number of frames.
     * 
     * @param audioFormat the audio format.
     * @param totalFrames the number of sample frames.
     */
    public Sample(AudioFormat audioFormat, long totalFrames) {
        this();
    	this.audioFormat = audioFormat;
        nChannels = audioFormat.getChannels();
        nFrames = totalFrames;
        sampleData = new byte[2*nChannels*(int)totalFrames]; //16-bit
        Arrays.fill(sampleData,(byte)0);
        length = totalFrames / audioFormat.getSampleRate() * 1000f;
    }
	
    /**
	 * Create a sample. Call setFile to initialise the sample.
 	 *
	 */
	public Sample()
	{
        bufferingRegime = BufferingRegime.TOTAL;
        isBigEndian = true;
        verbose = false;
	}
	
	/**
	 * Create a sample from a file. This constructor immediately loads the entire audio file into memory.
	 * If you want buffered behaviour, then use the blank constructor.
	 * @throws UnsupportedAudioFileException 
	 * @throws IOException 
	 */
	public Sample(String filename) throws IOException, UnsupportedAudioFileException
	{    	
		this();
		setFile(filename);
	}

	/**
	 * The buffering regime affects how the sample accesses the audio data.
	 * BufferingRegime.TOTAL: The default behaviour. The entire file is read into memory, providing fast access at the cost of more memory used.
	 * BufferingRegime.TIMED: Only some parts of the audio file are stored in memory. Useful for very large audio files. Various parameters affect the buffering behaviour.  
	 * 
	 * @param br The buffering regime to use.
	 */
	public void setBufferingRegime(Sample.BufferingRegime br)
	{
		//FIXME what if the sample has already been loaded? Can we switch regimes mid flow? 
		bufferingRegime = br;
	}

	/**
	 * Set how many milliseconds from last loaded point to look ahead.
	 * 
	 * @param lookahead time to look ahead in ms.
	 */
	public void setLookAhead(float lookahead) 
	{
		this.t_lookahead = (int)lookahead;
	}

	/**
	 * Set how many milliseconds from last loaded point to look backwards.
	 * 
	 * @param lookback time to look backwards in ms.
	 */
	public void setLookBack(float lookback) 
	{
		this.t_lookback = (int)lookback;
	}

	/**
	 * If a part of an audio file has not been accessed for some amount of time it is discarded.
	 * The time that the part remains in memory is specified by setMemory().
	 * Passing a value of -1 to this function will set the memory to the maximum value possible.
	 * 
	 * @param ms Duration in milliseconds that unaccessed regions remain loaded.  
	 */
	public void setMemory(float ms)
	{
		this.t_memory = (int)ms;
	}

	/**
	 * NOT YET IMPLEMENTED. Use setMemory() if RAM conservation is necessary.
	 * 
	 * Specify the maximum amount of memory this sample uses.
	 * If the sample is large this helps with conserving ram.
	 * If this is 0 then the space is unlimited.
	 * @param mb Size of buffer (in megabytes.) 
	 */
	/*
	public void setBufferMax(int mb)
	{
		this.t_bufferMax = mb;
	}*/

	/**
	 * Specify the size of each buffered region.
	 * 
	 * @param ms Size of the region (ms)
	 */
	public void setRegionSize(float ms)
	{
		this.t_regionSize = ms;
	}

	/**
	 * Specify an audio file that the Sample reads from.
	 * 
	 * If BufferedRegime is TOTAL, this will block until the sample is loaded.
	 * 
	 */
	public void setFile(String file) throws IOException, UnsupportedAudioFileException
	{
		audioFile = new AudioFile(file);
		setFile(audioFile);
	}
	
	/**
	 * Specify an explicit AudioFile that the Sample reads from.
	 * NOTE: Only one sample should reference a particular AudioFile.
	 * 
	 * If BufferedRegime is TOTAL, this will block until the sample is loaded.
	 * 
	 */
	public void setFile(AudioFile af) throws IOException, UnsupportedAudioFileException
	{
		audioFile = af;
		audioFile.open();

		audioFormat = audioFile.getDecodedFormat();
		nFrames = audioFile.nFrames;
		nChannels = audioFile.nChannels;
		length = audioFile.length;
		isBigEndian = audioFile.getDecodedFormat().isBigEndian();
		
		init();
	}

	/// set everything up, ready to use
	private void init() throws IOException
	{
		if (bufferingRegime==BufferingRegime.TOTAL)
		{
			// load all the sample data into a byte buffer
			loadEntireSample();			
			// lot of crap left over, so call the gc
			// TODO: should probably call gc() only once after ALL samples are loaded
			// Can do this in sample manager, for instance
			// Ollie: or two alternatives: advise user to do this, or AudioContext calls gc() before starting
			//which assumes that most samples are loaded before system is turned on.
			//On the other hand, this is probably fine since it is only a hint to the system.
			System.gc();
		}
		else if (bufferingRegime==BufferingRegime.TIMED)
		{
			if (nFrames==AudioSystem.NOT_SPECIFIED)
			{
				// TODO: Do a quick run through and guess the length?
				System.out.println("BufferedSample needs to know the length of the audio file it uses, but cannot determine the length.");
				System.exit(1);
			}
			
			// initialise params
			regionSize = (int)Math.ceil(((t_regionSize/1000.) * audioFile.getDecodedFormat().getSampleRate()));
			lookahead = (int)Math.ceil(((t_lookahead/1000.) * audioFile.getDecodedFormat().getSampleRate())/regionSize);
			lookback = (int)Math.ceil(((t_lookback/1000.) * audioFile.getDecodedFormat().getSampleRate())/regionSize);
			if (t_memory==-1) memory = Long.MAX_VALUE;			
			memory = t_memory;	
			regionSizeInBytes = regionSize * 2 * nChannels;
			numberOfRegions = 1 + (int)(nFrames / regionSize);
			
			/* TODO: IMPLEMENT THIS?
			if (t_bufferMax==0) maxRegionsLoadedAtOnce = 0;
			else
				maxRegionsLoadedAtOnce = (int)Math.ceil(1.0*t_bufferMax/regionSizeInBytes);
			*/
			
			if(verbose) System.out.printf("regionsize: %d frames,lookahead: %d regions ,lookback: %d regions, memory: %d ms\n",regionSize,lookahead,lookback,memory);
			
			// the last region may contain 0 to (regionSize-1) samples
			
			numberOfRegionsLoaded = 0;
			regions = new byte[numberOfRegions][];
			regionAge = new long[numberOfRegions];
			// a null region is a region that isn't loaded yet, or that has been discarded
			Arrays.fill(regions,null);
			Arrays.fill(regionAge,0);
						
			if(verbose) System.out.printf("Timed Sample has %d regions of %d bytes each.\n",numberOfRegions,regionSizeInBytes);
						
			// initialise region thread stuff
			regionQueue = new ConcurrentLinkedQueue<Integer>();
			regionQueued = new boolean[numberOfRegions];
			regionLocks = new Lock[numberOfRegions];
			for (int j=0;j<regionLocks.length;j++)
			{
				regionLocks[j] = new ReentrantLock();
			}
			//TODO might want to actually switch thread on and off
			regionThread = new Thread(this);
			regionThread.setDaemon(true);
			
			timeAtLastAgeUpdate = 0; //System.nanoTime()/1000;			
			regionThread.start();
						
			// regionThread.setPriority()
						
			// load first N regions ... just for testing...
			/*
			for(int i=0;i<Math.min(numberOfRegions,100);i++)
			{
				regions[i] = new byte[regionSizeInBytes];
				//audioFile.seek(i*regionSize); // note that seek is in frames!
				System.out.println("Loaded region " + i + "/" + numberOfRegions);
				int numBytes = audioFile.read(regions[i]);
				if (numBytes!=regionSizeInBytes)
				{
					System.out.println("Region incomplete! (" + numBytes + " bytes read)");
				}
			}		
			*/
		}
	}
	
//	/**
//	 * Returns a single frame given the time in milliseconds.
//	 * If the data is not readily available this function blocks until it is.
//	 * 
//	 * @param timeMs the time in milliseconds.
//	 */
//	public void getFrame(double timeMs, float[] frame) {
//		getFrame((int)msToSamples(timeMs), frame);
//	}

	/**
	 * Return a single frame. 
	 * If the data is not readily available this function blocks until it is.
	 *  
	 * @param frame Must be in range, else framedata is unchanged. 
	 * @param frameData
	 * 
	 */
	public void getFrame(int frame, float[] frameData)
	{
		if (frame >= nFrames) return;
		
		if (bufferingRegime==BufferingRegime.TOTAL)
		{
			int startIndex = frame * 2 * nChannels;
			AudioUtils.byteToFloat(frameData,sampleData,isBigEndian,startIndex,frameData.length);			
		}
		else if (bufferingRegime==BufferingRegime.TIMED)
		{
			int whichRegion = frame / regionSize;
			
			// When someone requests a region, it may not be loaded yet.
			// Alternatively it may currently be being deleted, in which case we have to wait.
			
			// lock access to region r, load it, and return it...
			// wait until it is free...		
			try {
				while (!regionLocks[whichRegion].tryLock(10, TimeUnit.MILLISECONDS)){}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				byte[] regionData = getRegion(whichRegion);
				if (regionData!=null)
				{
					// convert it to the correct format,
					int startIndex = (frame % regionSize) * 2 * nChannels;
					AudioUtils.byteToFloat(frameData,regionData,isBigEndian,startIndex,frameData.length);
				}
			}
			finally {
				regionLocks[whichRegion].unlock();
			}
		}
	}

	/**
	 * Get a series of frames. FrameData will only be filled with the available frames. 
	 * It is the caller's responsibility to count how many frames are valid.
	 * e.g., min(nFrames - frame, frameData[0].length) frames in frameData are valid.  
	 * 
	 * If the data is not readily available this function blocks until it is.
	 * 
	 * @param frame
	 * @param frameData
	 */
	public void getFrames(int frame, float[][] frameData)
	{
		if (frame >= nFrames) return;
		
		if (bufferingRegime==BufferingRegime.TOTAL)
		{
			int startIndex = frame * 2 * nChannels;			
			int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame))*nChannels;			
			float[] floatdata = new float[numFloats];
			AudioUtils.byteToFloat(floatdata, sampleData, isBigEndian, startIndex, numFloats);
			AudioUtils.deinterleave(floatdata,nChannels,frameData[0].length,frameData);
		}
		else if (bufferingRegime==BufferingRegime.TIMED)
		{
			int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame))*nChannels;			
			float[] floatdata = new float[numFloats];
			
			// fill floatdata with successive regions of byte data
			int floatdataindex = 0;			
			int regionindex = frame % regionSize;
			int whichregion = frame / regionSize;
			int numfloatstocopy = Math.min(regionSize - regionindex,numFloats - floatdataindex);
			
			while (numfloatstocopy>0)
			{
				// see getFrame() for explanation
				try {
					while (!regionLocks[whichregion].tryLock(10, TimeUnit.MILLISECONDS)){}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					byte[] regionData = getRegion(whichregion);
					if (regionData!=null)
						AudioUtils.byteToFloat(floatdata, regionData, isBigEndian, regionindex*2*nChannels, floatdataindex*nChannels, numfloatstocopy*nChannels);
				}
				finally {
					regionLocks[whichregion].unlock();
				}
				floatdataindex += numfloatstocopy;
				regionindex = 0;				
				numfloatstocopy = Math.min(regionSize,numFloats - floatdataindex);				
				whichregion++;
			}
			
			// deinterleave the whole thing			
			AudioUtils.deinterleave(floatdata,nChannels,frameData[0].length,frameData);
		}
	}	
	
	// Region loading, handling, queuing, removing, etc...	
	
	/// Region handling, loading, etc...
	// assume region r exists
	private byte[] getRegion(int r)
	{	
		// first determine whether the region is valid
		if (!isRegionAvailable(r))
		{
			loadRegion(r);
		}
		// then queue some regions, in the appropriate order
		for(int i=Math.max(0,r-lookback);i<Math.min(r+lookahead,numberOfRegions);i++)
		{
			if (i!=r) queueRegionForLoading(i);
		}
		// touch the region, make it new
		synchronized (regionAge)
		{	regionAge[r] = 0; }
		// then return it
		
		return regions[r];
	}
	
	private boolean isRegionAvailable(int r)
	{
		return regions[r]!=null;
	}
	
	private boolean isRegionQueued(int r)
	{
		return regionQueued[r];
	}
	
	/// loads the region IMMEDIATELY, blocks until it is loaded
	// this is called by the regionloader as it loads,
	// but also by the main thread when it needs a region RIGHT AWAY
	synchronized private void loadRegion(int r)
	{
		// for now, just seek to the correct position 
		try {			
			regions[r] = new byte[regionSizeInBytes];
			numberOfRegionsLoaded++;
			audioFile.seek(regionSize*r);
			int bytesRead = audioFile.read(regions[r]);
			if (bytesRead<=0)
				regions[r] = null;
			synchronized(regionAge)
			{
				regionAge[r] = 0;
			}
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	/// load the region r when you can, non-blocking
	private void queueRegionForLoading(int r)
	{
		if (!isRegionAvailable(r) && !isRegionQueued(r))
		{
			regionQueued[r] = true;
			regionQueue.add(r);
			
			// wake up the region thread master
			if (regionThread.getState()==State.TIMED_WAITING)
				regionThread.interrupt();
			
		}
	}

	public void run() {
		while (true)
		{		
			// if there's a region on the queue then load it
			boolean queuedregion = !regionQueue.isEmpty();
			
			if (queuedregion)
			{
				int r = regionQueue.poll();
				if (regionLocks[r].tryLock())
				{		
					try {
						if (regions[r]==null)
							loadRegion(r);							
						synchronized(regionQueued)
						{
							regionQueued[r] = false;
						}
					}
					finally {
						regionLocks[r].unlock();
					}
				}
			}
			
			// age all the loaded regions
			// remove the oldest ones if we exceed the memory limit		
			// TODO: don't need to age things all the time, this should be based on some tunable param
			if (timeAtLastAgeUpdate==0) timeAtLastAgeUpdate = System.currentTimeMillis();
			long dt = System.currentTimeMillis() - timeAtLastAgeUpdate;	
			//System.out.println(dt);
			//System.out.println();
			
			//int numRegionsToRemove = numberOfRegionsLoaded - maxRegionsLoadedAtOnce;
			//SortedSet sortedByAge = new TreeSet<Integer>(new Comparator(){});
			//if (numRegionsToRemove>0)
			for (int i=0;i<numberOfRegions;i++)
			{	
				//System.out.printf("%d ",regionAge[i]);
				if (regions[i]!=null)
				{
					synchronized(regionAge)
					{
						regionAge[i] += dt;
					}
					if (regionAge[i]>memory)
					{
						// if it is unlocked, then remove it...
						if (regionLocks[i].tryLock())
						{
							try {
								regions[i] = null;
								numberOfRegionsLoaded--;
							}
							finally {
								regionLocks[i].unlock();
							}
						}
						// else, ignore and try again next time...
					}
				}
			}
			timeAtLastAgeUpdate += dt;
			
			if (!queuedregion)
			{
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ignore) 
				{
					//System.out.println("Wake up!");
				}
			}
		}
	}
	
	// a helper function, loads the entire sample into sampleData
	private void loadEntireSample() throws IOException
	{
		final int BUFFERSIZE = 4096;
		byte[] audioBytes = new byte[BUFFERSIZE];

		int sampleBufferSize = 4096;
		sampleData = new byte[sampleBufferSize];
		int bytesRead;
		int totalBytesRead = 0;
		
		int numberOfFrames = 0;
		
		while ((bytesRead = audioFile.read(audioBytes))!=-1) {
			
			int numFramesJustRead = bytesRead / (2 * nChannels);
			
			// resize buf if necessary
			if (bytesRead > (sampleBufferSize-totalBytesRead))
			{
				sampleBufferSize = Math.max(sampleBufferSize*2, sampleBufferSize + bytesRead);
				//System.out.printf("Adjusted samplebuffersize to %d\n",sampleBufferSize);
				
				// resize buffer
				byte[] newBuf = new byte[sampleBufferSize];
				System.arraycopy(sampleData, 0, newBuf, 0, sampleData.length);					
				sampleData = newBuf;
			}
			
			System.arraycopy(audioBytes, 0, sampleData, totalBytesRead, bytesRead);			
			
			numberOfFrames += numFramesJustRead;
			totalBytesRead += bytesRead;
		}
		
		// resize buf to proper length
		// resize buf if necessary
		if (sampleBufferSize > totalBytesRead)
		{
			sampleBufferSize = totalBytesRead;
			
			// resize buffer				
			byte[] newBuf = new byte[sampleBufferSize];
			System.arraycopy(sampleData, 0, newBuf, 0, sampleBufferSize);					
			sampleData = newBuf;
		}
		
		nFrames = sampleBufferSize / (2*nChannels);
		
		audioFile.close();		
	}
	
	/**
     * Prints audio format info to System.out.
     */
    public void printAudioFormatInfo() {
        System.out.println("Sample Rate: " + audioFormat.getSampleRate());
        System.out.println("Channels: " + nChannels);
        System.out.println("Frame size in Bytes: " + audioFormat.getFrameSize());
        System.out.println("Encoding: " + audioFormat.getEncoding());
        System.out.println("Big Endian: " + audioFormat.isBigEndian());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return getFileName();
    }
    
    
    /**
     * Gets the full file path.
     * 
     * @return the file path.
     */
    public String getFileName() {
    	return audioFile.file.getAbsolutePath();
    }
    
    
    /**
     * Gets the simple file name.
     * 
     * @return the file name.
     */
    public String getSimpleFileName() {
    	return audioFile.file.getName();
    }
    
    /**
     * Converts from milliseconds to samples based on the sample rate specified by {@link #audioFormat}.
     * 
     * @param msTime the time in milliseconds.
     * 
     * @return the time in samples.
     */
    public double msToSamples(double msTime) {
        return msTime * audioFormat.getSampleRate() / 1000.0f;
    }

    /**
     * Converts from samples to milliseconds based on the sample rate specified by {@link #audioFormat}.
     * 
     * @param sampleTime the time in samples.
     * 
     * @return the time in milliseconds.
     */
    public double samplesToMs(double sampleTime) {
        return sampleTime / audioFormat.getSampleRate() * 1000.0f;
    }
    
    /**
     * Retrieves a frame of audio using linear interpolation.
     * 
     * @param currentSample the current sample.
     * @param fractionOffset the offset from the current sample as a fraction of the time
     * to the next sample.
     * 
     * @return the interpolated frame.
     */
    public float[] getFrameLinear(int currentSample, float fractionOffset) {
        float[] result = new float[nChannels];
    	if(currentSample >= 0 && currentSample < nFrames) {
    		if (currentSample == nFrames-1)
    		{
    			getFrame(currentSample,result);    			
    		}
    		else
    		{
	    		float[] current = new float[nChannels];    		
	    		getFrame(currentSample,current);
	    		float[] next = new float[nChannels];
	    		getFrame(currentSample+1,next);
	    		
	    		for (int i = 0; i < nChannels; i++) {
	    			result[i] = (1 - fractionOffset) * current[i] +
                        fractionOffset * next[i];
	    		}   
            }
        } else {
             for(int i = 0; i < nChannels; i++) {
                 result[i] = 0.0f;
             }
        }
        return result;
    }
    
    /**
     * Retrieves a frame of audio using cubic interpolation.
     * 
     * @param currentSample the current sample.
     * @param fractionOffset the offset from the current sample as a fraction of the time
     * to the next sample.
     * 
     * @return the interpolated frame.
     */
    public float[] getFrameCubic(int currentSample, float fractionOffset) {    	
    	System.out.println("Cubic interpolation not yet supported.\n");
    	System.exit(1);
    	return null;
    	/*
    	 * OLLIES OLD CUBIC INTERPOLATION CODE
        float[] result = new float[nChannels];
        float a0,a1,a2,a3,mu2;
        float ym1,y0,y1,y2;
        for (int i = 0; i < nChannels; i++) {
            int realCurrentSample = currentSample;
            if(realCurrentSample >= 0 && realCurrentSample < (nFrames - 1)) {
                realCurrentSample--;
                if (realCurrentSample < 0) {
                    ym1 = buf[i][0];
                    realCurrentSample = 0;
                } else {
                    ym1 = buf[i][realCurrentSample++];
                }
                y0 = buf[i][realCurrentSample++];
                if (realCurrentSample >= nFrames) {
                    y1 = buf[i][(int)nFrames - 1]; //??
                } else {
                    y1 = buf[i][realCurrentSample++];
                }
                if (realCurrentSample >= nFrames) {
                    y2 = buf[i][(int)nFrames - 1]; //??
                } else {
                    y2 = buf[i][realCurrentSample];
                }
                mu2 = fractionOffset * fractionOffset;
                a0 = y2 - y1 - ym1 + y0;
                a1 = ym1 - y0 - a0;
                a2 = y1 - ym1;
                a3 = y0;
                result[i] = a0 * fractionOffset * mu2 + a1 * mu2 + a2 * fractionOffset + a3;
            } else {
                result[i] = 0.0f;
            }
        }
        return result;
        */
    }
    
    /** 
	 * A Sample needs to be writeable in order to be recorded into.
	 * Currently buffered samples are not writeable, but TOTAL (file or empty) samples are.
	 */
	public boolean isWriteable()
	{
		return bufferingRegime==BufferingRegime.TOTAL;
	}
	
	/**
	 * Write a single frame into this sample. Takes care of format conversion.
	 * 
	 * This only makes sense if this.isWriteable() returns true.
	 * If isWriteable() is false, the behaviour is undefined/unstable.
	 * 
	 * @param frame The frame to write into.
	 * @param frameData The frame data to write.
	 */
	public void putFrame(int frame, float[] frameData)
	{
		int startIndex = frame * 2 * nChannels;
		AudioUtils.floatToByte(sampleData, startIndex, frameData, 0, frameData.length, isBigEndian);					
	}
	
	/**
	 * Write multiple frames data into the sample.
	 * 
	 * This only makes sense if this.isWriteable() returns true.
	 * If isWriteable() is false, the behaviour is undefined/unstable.
	 * 
	 * @param frame The frame to write into.
	 * @param frameData The frames to write.
	 */
	public void putFrames(int frame, float[][] frameData)
	{
		int startIndex = frame * 2 * nChannels;			
		int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame))*nChannels;			
		
		float[] floatdata = new float[numFloats];
		AudioUtils.interleave(frameData,nChannels,frameData[0].length,floatdata);
		AudioUtils.floatToByte(sampleData, startIndex, floatdata, 0, floatdata.length, isBigEndian);
	}
	
	/**
	 * Write multiple frames data into the sample.
	 * 
	 * This only makes sense if this.isWriteable() returns true.
	 * If isWriteable() is false, the behaviour is undefined/unstable.
	 * 
	 * @param frame The frame to write into.
	 * @param frameData The frames to write.
	 * @param offset The offset into frameData
	 * @param numFrames The number of frames from frameData to write
	 */
	public void putFrames(int frame, float[][] frameData, int offset, int numFrames)
	{
		if (numFrames<=0) return;
		
		int startIndex = frame * 2 * nChannels;			
		int numFloats = Math.min(numFrames,(int)(nFrames-frame))*nChannels;			
		
		float[] floatdata = new float[numFloats];
		AudioUtils.interleave(frameData,nChannels,offset,frameData[0].length,floatdata);
		AudioUtils.floatToByte(sampleData, startIndex, floatdata, 0, floatdata.length, isBigEndian);
	}
    
    /**
     * Write Sample to a file.
     * This will record the entire sample to a file.
     * BLOCKING.
     * 
     * @param fn the file name.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(String fn) throws IOException {
    	if (bufferingRegime==BufferingRegime.TOTAL)
    	{
    		ByteArrayInputStream bais = new ByteArrayInputStream(sampleData);
            AudioInputStream aos = new AudioInputStream(bais, audioFormat, nFrames);
            AudioSystem.write(aos, AudioFileFormat.Type.AIFF, new File(fn));
    	}
    	else if (bufferingRegime==BufferingRegime.TIMED)
    	{
    		System.out.println("Writing buffered samples to disk is not yet supported.");
    		System.exit(1);
    		
    		/*
    		// for each region, load it if necessary and write out the data
    		File file = new File(fn);
    		for(int i=0;i<numberOfRegions;i++)
    		{
    			try {
					while (!regionLocks[i].tryLock(10, TimeUnit.MILLISECONDS)){}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					byte[] regionData = getRegion(i);
					if (regionData!=null)
					{
						ByteArrayInputStream bais = new ByteArrayInputStream(regionData);
						AudioInputStream aos = new AudioInputStream(bais, audioFormat, regionSize);
			            AudioSystem.write(aos, AudioFileFormat.Type.AIFF, file);
					}
				}
				finally {
					regionLocks[i].unlock();
				}
    		}
    		*/
    	}
    	
        
    }

	
	public AudioFile getAudioFile() {
		return audioFile;
	}

	
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	
	public int getNumChannels() {
		return nChannels;
	}

	
	public long getNumFrames() {
		return nFrames;
	}

	
	public float getLength() {
		return length;
	}

	public float getSampleRate() {
		return audioFormat.getSampleRate();
	}
    
    public int getNumberOfRegionsLoaded() {
    	return numberOfRegionsLoaded;
    }
    
}
