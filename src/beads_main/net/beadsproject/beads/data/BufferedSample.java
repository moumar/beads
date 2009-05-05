package net.beadsproject.beads.data;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.beadsproject.beads.core.AudioUtils;

/**
 * A Buffered Sample provides random access to an audio file. 
 * 
 * Only supports 16-bit samples at the moment.
 * TODO: getFrameAtTime(t) with interpolation? 
 * 
 * Notes:
 * - BufferedSample is not particularly thread-safe. 
 *   This shouldn't matter because SamplePlayers are not executed in parallel 
 *   when used in a UGen chain.
 *    
 * @author ben
 */
public class BufferedSample implements Runnable {

	public enum BufferingRegime {
		TIMED,
		TOTAL
	};
	public BufferingRegime bufferingRegime = BufferingRegime.TOTAL;

	/** Store the sample data with native bit-depth. 
	 * Storing in native bit-depth reduces memory load, but increases cpu-load. 
	 */ 
	//public boolean storeInNativeBitDepth = true;	
	
	private AudioFile audioFile;

	/** The audio format. */
	public AudioFormat audioFormat;

	/** The number of channels. */
	public int nChannels;

	/** The number of sample frames. */
	public long nFrames;

	/** The length in milliseconds. */
	public float length;	

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
	public int numberOfRegionsLoaded; // number of loaded regions
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
	 * Create a sample with default parameters.
	 * At this point, the sample contains no data. The data must be set using setFile().
	 * 
	 */
	public BufferedSample()
	{    	

	}

	/**
	 * The buffering regime affects how the sample accesses the audio data.
	 * BufferingRegime.TOTAL: The default behaviour. The entire file is read into memory, providing fast access at the cost of more memory used.
	 * BufferingRegime.TIMED: Only some parts of the audio file are stored in memory. Useful for very large audio files. Various parameters affect the buffering behaviour.  
	 * 
	 * @param br The buffering regime to use.
	 */
	public void setBufferingRegime(BufferedSample.BufferingRegime br)
	{
		bufferingRegime = br;
	}

	//set how many milliseconds from last loaded point to look ahead
	public void setLookAhead(int lookahead) 
	{
		this.t_lookahead = lookahead;
	}

	//set how many milliseconds  from last loaded  point to look back
	public void setLookBack(int lookback) 
	{
		this.t_lookback = lookback;
	}

	/**
	 * If a part of an audio file has not been accessed for some amount of time it is discarded.
	 * The time that the part remains in memory is specified by setMemory().
	 * Passing a value of -1 to this function will set the memory to the maximum value possible.
	 * 
	 * @param ms Duration in milliseconds that unaccessed regions remain loaded.  
	 */
	public void setMemory(int ms)
	{
		this.t_memory = ms;
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
		audioFile.open();
		
		nFrames = audioFile.nFrames;
		nChannels = audioFile.nChannels;
		
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
			// Can do this in sample manager
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
			
			System.out.printf("regionsize: %d frames,lookahead: %d regions ,lookback: %d regions, memory: %d ms\n",regionSize,lookahead,lookback,memory);
			
			// the last region may contain 0 to (regionSize-1) samples
			
			numberOfRegionsLoaded = 0;
			regions = new byte[numberOfRegions][];
			regionAge = new long[numberOfRegions];
			// a null region is a region that isn't loaded yet, or that has been discarded
			Arrays.fill(regions,null);
			Arrays.fill(regionAge,0);
						
			System.out.printf("Timed Sample has %d regions of %d bytes each.\n",numberOfRegions,regionSizeInBytes);
						
			// initialise region thread stuff
			regionQueue = new ConcurrentLinkedQueue<Integer>();
			regionQueued = new boolean[numberOfRegions];
			regionLocks = new Lock[numberOfRegions];
			for (int j=0;j<regionLocks.length;j++)
			{
				regionLocks[j] = new ReentrantLock();
			}
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
			AudioUtils.byteToFloat(frameData,sampleData,audioFile.getDecodedFormat().isBigEndian(),startIndex,frameData.length);			
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
				// convert it to the correct format,
				int startIndex = (frame % regionSize) * 2 * nChannels;
				AudioUtils.byteToFloat(frameData,regionData,audioFile.getDecodedFormat().isBigEndian(),startIndex,frameData.length);			
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
			AudioUtils.byteToFloat(floatdata, sampleData, audioFile.getDecodedFormat().isBigEndian(), startIndex, numFloats);
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
					AudioUtils.byteToFloat(floatdata, regionData, audioFile.getDecodedFormat().isBigEndian(), regionindex*2*nChannels, floatdataindex*nChannels, numfloatstocopy*nChannels);
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
			audioFile.read(regions[r]);
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

}
