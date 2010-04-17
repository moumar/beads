package net.beadsproject.beads.data.sample;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFileFormat;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.data.AudioFile;
import net.beadsproject.beads.data.AudioFile.AudioFileUnsupportedException;


/**
 * EXPERIMENTAL: A sample that reduces memory usage by streaming sample data from disk. 
 * 
 * An additional feature is that chunks of data can be garbage collected if they haven't been accessed 
 * in a specified amount of time.
 * 
 * The parameters of the buffering behaviour are controlled by supplying a TimedRegime to the constructor.
 * 
 * @author ben
 */
public class ExperimentalStreamingSample extends Sample implements Runnable{
	
	static public class Regime
	{
		/** 
		 * Store the sample data in the native bit format.
		 * 
		 * If true then memory is conserved, but a conversion has to be done every
		 * time sample data is requested.
		 * 
		 * If false (the default) then the sample is stored in the internal
		 * format used by Beads. It uses more memory but is faster.
		 */
		public boolean storeInNativeBitDepth;
	
		private Regime()
		{
			this(false);
		}
	
		private Regime(boolean nbp)
		{
			storeInNativeBitDepth = nbp;
		}
					
		/**
		 * Stream the file from disk -- storing the data in the sample. Useful for a forward sample player
		 * that possibly skips to different parts of the file. Once data is buffered it remains around forever.
		 * 
		 * @param regionSize Buffer size in ms. Generally should be a small fraction of the entire file. 
		 */
		static public TimedRegime newStreamingRegime(long regionSize)
		{
			return new TimedRegime(regionSize,regionSize*2,0,-1,TimedRegime.Order.ORDERED);
		} 
		
		/**
		 * Like {@link Sample.Regime#newStreamingRegime(long)} but each buffered segment is discarded after a specified
		 * amount of time. This is useful if the sample is very large to conserve memory.
		 * 
		 * @param regionSize Buffer size in ms. Generally should be a small fraction of the entire file. 
		 * @param memory Amount of time (ms) that an untouched region is allowed to live for.
		 */
		static public TimedRegime newStreamingRegimeWithAging(long regionSize, long memory)
		{
			return new TimedRegime(regionSize,regionSize*2,0,memory,TimedRegime.Order.ORDERED);
		}
	};
	
	/**
	 * <p>
	 * A TimedRegime stores only some parts of the audio file in memory at a time.
	 * It is useful for very large audio files, for audio streaming (e.g., playing an mp3),
	 * or for samples of which only a small part is used.
	 * </p>
	 * <p>
	 * A sample with a TimedRegime loads "regions" of sample data and buffers them according
	 * to various parameters. See the method documentation for more details, but briefly, the parameters are:
	 * <ul>
	 * <li>{@link #regionSize}: The size of the region in ms.</li>
	 * <li>{@link #lookAhead}, {@link #lookBack}: When a region is accessed the lookAhead and lookBack determine which surrounding regions should be queued and loaded.</li>
	 * <li>{@link #memory}: The amount of time an unaccessed region should be kept in memory.</li>
	 * <li>{@link #loadingOrder}: Affects the order that surrounding regions are queued. NEAREST is suitable if you are playing backwards and forwards around a sample position, while ORDERED is suitable for playing forwards.</li>
	 * </ul>
	 * </p>
	 */  
	static public class TimedRegime extends Regime
	{
		public long lookAhead; // time lookahead, ms
		public long lookBack; // time lookback, ms 
		public long memory;   // age (ms) at which regions get removed		
		public long regionSize; // size of each region (by default 10s)
	
		static public enum Order {
			NEAREST,
			ORDERED
		};
	
		public Order loadingOrder;
	
		public TimedRegime()
		{
			super();
			lookAhead = 100;
			lookBack = 0;
			memory = 1000;
			regionSize = 100;
			loadingOrder = Order.ORDERED;
		}
	
		public TimedRegime(long regionSize,
				long lookAhead,
				long lookBack,
				long memory,
				Order loadingOrder)
		{
			this(regionSize,lookAhead,lookBack,memory,loadingOrder,false);
		}
	
		public TimedRegime(long regionSize, 
				long lookAhead, 
				long lookBack, 
				long memory, 
				Order loadingOrder,
				boolean storeInNativeBitDepth)
		{
			super(storeInNativeBitDepth);
			this.regionSize = regionSize;
			this.lookAhead = lookAhead;
			this.lookBack = lookBack;
			this.memory = memory;
			this.loadingOrder = loadingOrder;
		}
	
		/**
		 * Set how many milliseconds from last loaded point to look ahead.
		 * 
		 * @param lookahead time to look ahead in ms.
		 */
		public void setLookAhead(long lookahead) 
		{
			this.lookAhead = lookahead;
		}
	
		/**
		 * Set how many milliseconds from last loaded point to look backwards.
		 * 
		 * @param lookback time to look backwards in ms.
		 */
		public void setLookBack(long lookback) 
		{
			this.lookBack = lookback;
		}
	
		/**
		 * If a part of an audio file has not been accessed for some amount of time it is discarded.
		 * The time that the part remains in memory is specified by setMemory().
		 * Passing a value of -1 to this function will set the memory to the maximum value possible.
		 * 
		 * @param ms Duration in milliseconds that unaccessed regions remain loaded.  
		 */
		public void setMemory(long ms)
		{
			this.memory = ms;
		}
	
		/**
		 * Specify the size of each buffered region.
		 * 
		 * @param ms Size of the region (ms)
		 */
		public void setRegionSize(long ms)
		{
			this.regionSize = ms;
		}
	
		/**
		 * When a region is loaded, nearby regions are put on a queue to be 
		 * loaded also. The loading regime affects the order in which the nearby 
		 * regions (defined by lookback and lookahead) are loaded.
		 * 
		 * NEAREST (the default) will load regions nearest to the region first.
		 * ORDERED will load the regions from lowest to highest.
		 * 
		 * NEAREST makes sense if you are accessing the near regions first, e.g., playing a sample backwards or forwards.
		 * ORDERED makes sense if you are accessing random nearby regions. Loading regions in order is generally quicker.
		 * 
		 * @param lr The order to load regions.
		 */
		public void setLoadingRegime(Order lr)
		{
			this.loadingOrder = lr;
		}
	};
	
	// Sample stuff
	private Regime bufferingRegime;
	private boolean isBigEndian;
	
	/// The region master controls the loading of queued regions.
	static public Executor regionMaster = null;

	private int r_regionSize; // region size in frames
	private int r_lookahead; // num region lookahead
	private int r_lookback; // num regions lookback
	private long r_memory;

	private int regionSizeInBytes; // the number of bytes per region (regionSize * getNumChannels() * bitconversionfactor)
	private int numberOfRegions; // total number of regions
	private int numberOfRegionsLoaded; // number of loaded regions

	private byte[][] regions; // the actual data
	private float[][][] f_regions; // uninterleaved data

	private long[] regionAge; // the age of each region (in ms)
	private long timeAtLastAgeUpdate; // the time at the last age updated operation

	private boolean[] regionQueued; // true if a region is currently queued
	private ConcurrentLinkedQueue<Integer> regionQueue; // a queue of regions to be loaded
	private Lock[] regionLocks; // to support safe deletion/writing of regions
	
	// is this sample scheduled for region loading?
	private boolean isScheduled;
	
	public ExperimentalStreamingSample(AudioContext ac, String filename, Regime bufferingRegime) throws IOException, AudioFileUnsupportedException
	{   
		super(ac);
		this.bufferingRegime = bufferingRegime;
		setFile(filename);
	}
	
	public ExperimentalStreamingSample(AudioContext ac, String filename) throws IOException, AudioFileUnsupportedException
	{   
		super(ac);
		this.bufferingRegime = Regime.newStreamingRegime(1000);
		setFile(filename);
	}
		
	/**
	 * Return a single frame. 
	 * 
	 * If the data is not readily available this doesn't do anything to frameData.
	 *  
	 * @param frame Must be in range, else framedata is unchanged. 
	 * @param frameData
	 * 
	 */
	public void getFrame(int frame, float[] frameData)
	{
		if (frame<0 || frame >= nFrames) return;
	
		{
			int whichRegion = frame / r_regionSize;
			if (whichRegion > numberOfRegions)
			{
				Arrays.fill(frameData,0.f);		
				return;
			}	
	
			// When someone requests a region, it may not be loaded yet.
			// Alternatively it may currently be being deleted, in which case we have to wait.
	
			// lock access to region r, load it, and return it...
			// wait until it is free...		
			// TODO: 
			/*
			try {
				while (!regionLocks[whichRegion].tryLock(0, TimeUnit.MILLISECONDS))
				{}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
			
			try {
				if (regionLocks[whichRegion].tryLock(0, TimeUnit.MILLISECONDS))
				{
					try {
	
						if (bufferingRegime.storeInNativeBitDepth)
						{									
							byte[] regionData = getRegion(whichRegion);
							if (regionData!=null)
							{
								// convert it to the correct format,
								int startIndex = (frame % r_regionSize) * 2 * getNumChannels();
								AudioUtils.byteToFloat(frameData,regionData,isBigEndian,startIndex,frameData.length);
							}
						}
						else
						{
							float[][] regionData = getRegionF(whichRegion);
							if (regionData!=null)
							{
								int startIndex = frame % r_regionSize;
								for(int i=0;i<getNumChannels();i++)
									frameData[i] = regionData[i][startIndex];
							}
						}
					}
					catch (Exception e)
					{
						Arrays.fill(frameData,0.f);		
						return;
					}
					finally {
						regionLocks[whichRegion].unlock();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				Arrays.fill(frameData,0.f);		
				return;
			}
		}
	}	
	
	/**
	 * Get a series of frames. FrameData will only be filled with the available frames. 
	 * It is the caller's responsibility to count how many frames are valid.
	 * <code>min(nFrames - frame, frameData[0].length)</code> frames in frameData are valid.  
	 * 
	 * If the data is not readily available this doesn't do anything.
	 * 
	 * @param frame The frame number (NOTE: This parameter is in frames, not in ms!)
	 * @param frameData
	 */
	public void getFrames(int frame, float[][] frameData)
	{
		if (frame >= nFrames) return;
		
		{
			int whichregion = frame / r_regionSize;
			if (whichregion > numberOfRegions)
			{
				for(int i=0;i<getNumChannels();i++)
				{
					Arrays.fill(frameData[i],0.f);
				}
				return;
			}
			
			int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame));			
	
			float[] floatdata = null;
			if (bufferingRegime.storeInNativeBitDepth)
				floatdata = new float[numFloats*getNumChannels()];
	
			// fill floatdata with successive regions of byte data
			int floatdataindex = 0;
			int regionindex = frame % r_regionSize;
			int numfloatstocopy = Math.min(r_regionSize - regionindex, numFloats - floatdataindex);
	
			while (numfloatstocopy>0)
			{
				// see getFrame() for explanation
				/*
				try {
					while (!regionLocks[whichregion].tryLock(0, TimeUnit.MILLISECONDS)){}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				*/
				
				try
				{
					if (regionLocks[whichregion].tryLock(0, TimeUnit.MILLISECONDS))
					{
						try {
		
							if (bufferingRegime.storeInNativeBitDepth)
							{					
								byte[] regionData = getRegion(whichregion);
								if (regionData!=null)
									AudioUtils.byteToFloat(floatdata, regionData, isBigEndian, regionindex*2*getNumChannels(), floatdataindex*getNumChannels(), numfloatstocopy*getNumChannels());
								else
								{
									int start = floatdataindex*getNumChannels();
									for(int i=start; i<start+numfloatstocopy*getNumChannels();i++)
										floatdata[i] = 0.f;
								}						
							}
							else
							{
								float[][] regionData = getRegionF(whichregion);
								if (regionData!=null)
								{
									// copy all channels...
									for(int i=0;i<getNumChannels();i++)
									{
										System.arraycopy(regionData[i], 0, frameData[i], floatdataindex, numfloatstocopy);
									}
								}
								else
								{
									for(int i=0;i<getNumChannels();i++)
									{
										for(int f=floatdataindex; f<floatdataindex+numfloatstocopy; f++)
											frameData[i][f] = 0.f;
									}
								}
							}
						}
						catch (Exception e)
						{
							for(int i=0;i<getNumChannels();i++)
							{
								Arrays.fill(frameData[i],0.f);
							}
							return;
						}
						finally {
							regionLocks[whichregion].unlock();
						}
						floatdataindex += numfloatstocopy;
						regionindex = 0;				
						numfloatstocopy = Math.min(r_regionSize, numFloats - floatdataindex);				
						whichregion++;
					}
				}
				catch (InterruptedException e)
				{
					for(int i=0;i<getNumChannels();i++)
					{
						Arrays.fill(frameData[i],0.f);
					}
					return;
				}
			}
	
			if (bufferingRegime.storeInNativeBitDepth)
			{
				// deinterleave the whole thing			
				AudioUtils.deinterleave(floatdata,getNumChannels(),frameData[0].length,frameData);
			}		
		}
	}	
	
	/**
	 * @return The number of regions. 
	 */
	public int getNumberOfRegions() {
		return numberOfRegions;
	}
	
	/**
	 * @return The number of regions currently loaded. 
	 */
	public int getNumberOfRegionsLoaded() {
		return numberOfRegionsLoaded;
	}
	
	/**
	 * The buffering regime affects how the sample accesses the audio data.
	 * 
	 * 
	 * @param r The buffering regime to use.
	 */
	private void setBufferingRegime(Regime r)
	{
		bufferingRegime = r;
	}
	
	/**
	 * Specify an audio file that the Sample reads from.
	 * 
	 * If BufferedRegime is TOTAL, this will block until the sample is loaded.
	 * @throws AudioFileUnsupportedException 
	 * 
	 */
	private void setFile(String file) throws IOException, AudioFileUnsupportedException
	{
		audioFile = getContext().getAudioIO().getAudioFile(file);
		setFile(audioFile);
	}
	
	/**
	 * Specify an audio file that the Sample reads from.
	 * 
	 * If BufferedRegime is TOTAL, this will block until the sample is loaded.
	 * @throws AudioFileUnsupportedException 
	 * 
	 */
	private void setFile(InputStream is) throws IOException, AudioFileUnsupportedException
	{
		audioFile = getContext().getAudioIO().getAudioFile(is);
		setFile(audioFile);
	}
	
	/**
	 * Specify an explicit AudioFile that the Sample reads from.
	 * NOTE: Only one sample should reference a particular AudioFile.
	 * 
	 * If BufferedRegime is TOTAL, this will block until the sample is loaded.
	 * @throws AudioFileUnsupportedException 
	 * 
	 */
	private void setFile(AudioFile af) throws IOException, AudioFileUnsupportedException
	{
		audioFile = af;
		audioFile.open();
	
		audioFormat = audioFile.getFormat();
		nFrames = audioFile.getNumFrames();
		length = audioFile.getLength();
		isBigEndian = audioFile.getFormat().isBigEndian();
	
		init();
	}
	
	/// set everything up, ready to use
	private void init() throws IOException, AudioFileUnsupportedException
	{			
		{
			if (nFrames==-1)
			{
				// TODO: Do a quick run through and guess the length?				
				throw(new AudioFile.AudioFileUnsupportedException("ExperimentalStreamingSample cannot determine the length of the audio file for buffering. \n" +
						"Use a BufferedSample instead."));					
			}		
			else
			{	
				TimedRegime tr = (TimedRegime) bufferingRegime;
				
				// initialise params
				
				// if region size is greated than audio length then we clip the region size...
				// but we still keep it as a timed regime, with 0 lookback and 0 lookahead
				r_regionSize = (int)Math.ceil(((tr.regionSize/1000.) * audioFile.getFormat().getSampleRate()));
				if (r_regionSize>nFrames)
				{
					r_regionSize = (int) nFrames;
					r_lookahead = 0;
					r_lookback = 0;
					numberOfRegions = 1;
				}
				else
				{			
					// lookahead, lookback is always a multiple of regionSize
					if (tr.lookAhead<=0)
						r_lookahead = 0;
					else
						r_lookahead = 1 + (int) ((tr.lookAhead-1)/tr.regionSize);
					
					if (tr.lookBack<=0)
						r_lookback = 0;
					else
						r_lookback = 1 + (int) ((tr.lookBack-1)/tr.regionSize);
					
					numberOfRegions = 1 + (int) (nFrames / r_regionSize);
				}
				
				if (tr.memory==-1) 
					r_memory = Long.MAX_VALUE;			
				else 
					r_memory = tr.memory;	
				regionSizeInBytes = r_regionSize * 2 * getNumChannels();			
		
				// the last region may contain 0 to (regionSize-1) samples	
				numberOfRegionsLoaded = 0;
		
				if (bufferingRegime.storeInNativeBitDepth)
				{
					regions = new byte[numberOfRegions][];
					Arrays.fill(regions,null);
				}
				else
				{
					f_regions = new float[numberOfRegions][][];
					Arrays.fill(f_regions,null);
				}
		
				regionAge = new long[numberOfRegions];
				Arrays.fill(regionAge,0);
		
				// initialise region thread stuff
				regionQueue = new ConcurrentLinkedQueue<Integer>();
				regionQueued = new boolean[numberOfRegions];
				regionLocks = new Lock[numberOfRegions];
				for (int j=0;j<regionLocks.length;j++)
				{
					regionLocks[j] = new ReentrantLock();
					regionQueued[j] = false;
				}
				
				timeAtLastAgeUpdate = 0;
				isScheduled = false;
				
				if (regionMaster==null)
					regionMaster = Executors.newFixedThreadPool(1, new ThreadFactory()
						{
							public Thread newThread(Runnable r)
							{
								Thread t = new Thread(r); 
								t.setDaemon(true); 
								t.setPriority(Thread.MIN_PRIORITY); 
								return t;
							}
						}
					);
			}
		}
	}
	
	/// Region handling, loading, etc...
	private byte[] getRegion(int r)
	{	
		if (!isRegionAvailable(r))
		{
			queueRegionForLoading(r);
			queueRegions(r);
			return null;
		}
		else
		{
			queueRegions(r);
			touchRegion(r);
			return regions[r];
		}
	}
	
	/// Region handling, loading, etc...
	private float[][] getRegionF(int r)
	{	
		if (!isRegionAvailable(r))
		{
			queueRegionForLoading(r);
			queueRegions(r);
			// System.out.println("null");
			return null;
		}
		else
		{
			queueRegions(r);
			touchRegion(r);			
			return f_regions[r];
		}
	}
	
	private void queueRegions(int r)
	{
		if (((TimedRegime)bufferingRegime).loadingOrder==TimedRegime.Order.ORDERED)
		{
			// queue the regions from back to front
			for(int i=Math.max(0,r-r_lookback);i<=Math.min(r+r_lookahead,numberOfRegions-1);i++)
			{
				if (i!=r)
				{
					queueRegionForLoading(i);
					touchRegion(i);
				}
			}
		}
		else // loadingOrder==LoadingRegime.NEAREST
		{
			// queue the regions from nearest to furthest, back to front...
			int br = Math.min(r,r_lookback); // number of back regions
			int fr = Math.min(r_lookahead,numberOfRegions-1-r); // number of ahead regions
	
			// have two pointers, one going backwards the other going forwards			
			int bp = 1;
			int fp = 1;
			boolean backwards = (bp<=br); // start backwards (if there are backward regions)			
			while(bp<=br || fp<=fr)
			{
				if (backwards)
				{
					queueRegionForLoading(r-bp);
					touchRegion(r-bp);
					bp++;
					if (fp<=fr) backwards = false;
				}
				else // if forwards
				{
					queueRegionForLoading(r+fp);
					touchRegion(r+fp);
					fp++;
					if (bp<=br) backwards = true;
				}				
			}
		}
	}
	
	private void touchRegion(int r)
	{
		// touch the region, make it new
		// synchronized (regionAge)
		// {	regionAge[r] = 0; }
		//regionAge[r] = 0;
	}
	
	public boolean isRegionAvailable(int r)
	{
		if (bufferingRegime.storeInNativeBitDepth)
			return regions[r]!=null;
		else
			return f_regions[r]!=null;
	}
	
	public boolean isRegionQueued(int r)
	{
		return regionQueued[r];
	}
	
	/// loads the region IMMEDIATELY, blocks until it is loaded
	// this is called by the regionloader as it loads,
	// but also by the main thread when it needs a region RIGHT AWAY
	// 
	// TODO: Need a way to kill the regionMaster
	synchronized private void loadRegion(int r)
	{	
		// for now, just seek to the correct position 
		try {			
			if (bufferingRegime.storeInNativeBitDepth)
			{
				regions[r] = new byte[regionSizeInBytes];
				numberOfRegionsLoaded++;
				audioFile.seek(r_regionSize*r);
				int bytesRead = audioFile.read(regions[r]);
				if (bytesRead<=0)
					regions[r] = null;
			}
			else // store in float[][] format
			{
				// load the bytes and convert them on the spot
				byte[] region = new byte[regionSizeInBytes];
				numberOfRegionsLoaded++;
				audioFile.seek(r_regionSize*r);
				int bytesRead = audioFile.read(region);
				if (bytesRead<=0)
				{
					f_regions[r] = new float[getNumChannels()][r_regionSize];
					for(int i=0;i<getNumChannels();i++)
						Arrays.fill(f_regions[r][i],0.f);
				}
				else
				{	
					// now convert
					f_regions[r] = new float[getNumChannels()][r_regionSize];
					float[] interleaved = new float[getNumChannels()*r_regionSize];
					AudioUtils.byteToFloat(interleaved, region, isBigEndian);	
					AudioUtils.deinterleave(interleaved, getNumChannels(), r_regionSize, f_regions[r]);
				}
			}
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
			
			// if not scheduled then schedule
			rescheduleSelf();
		}
	}
	
	private void unloadRegion(int r)
	{
		if (bufferingRegime.storeInNativeBitDepth)
			regions[r]=null;
		else
			f_regions[r]=null;		
	}
	
	/**
	 * <i>Internal:</i> This method is called internally by the region master. It loads the next queued region. 
	 */
	public synchronized void run() {
		boolean hasMoreQueuedRegions = loadQueuedRegion();
		
		ageAllRegions();
		
		if (hasMoreQueuedRegions)
		{
			isScheduled = false;
			rescheduleSelf();
		}
		else
			isScheduled = false;
	}
	
	/* old run operation
	public void run() {
		while (true)
		{
			boolean hasMoreQueuedRegions = loadQueuedRegion();
	
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
				if (!isRegionAvailable(i))
				{
					synchronized(regionAge)
					{
						regionAge[i] += dt;
					}
					if (regionAge[i]>r_memory)
					{
						// if it is unlocked, then remove it...
						if (regionLocks[i].tryLock())
						{
							try {
								unloadRegion(i);
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
	
			if (!hasMoreQueuedRegions)
			{
				try {
					// Thread.currentThread()
					Thread.sleep(1000000);
				} catch (InterruptedException ignore) 
				{
					//System.out.println("Wake up!");
				}
			}
		}
	}
	*/
	
	/*
	 * Loads the next queued region. Returns true if there are more regions queued.
	 */
	private boolean loadQueuedRegion()
	{
		// First check if the queue has anything to load.
		if (!regionQueue.isEmpty())
		{
			// Load the next region
			int r = regionQueue.poll();
			if (regionLocks[r].tryLock())
			{		
				try {
					if (!isRegionAvailable(r)) 
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
		
		return !regionQueue.isEmpty();		
	}
	
	/*
	 * ages all the regions
	 * remove the oldest ones if we exceed the memory limit
	 * TODO: don't need to age things all the time, this should be based on some tunable param
	 */
	private void ageAllRegions()
	{
		if (timeAtLastAgeUpdate==0) 
			timeAtLastAgeUpdate = System.currentTimeMillis();
		long dt = System.currentTimeMillis() - timeAtLastAgeUpdate;	
		
		// we only update ages every multiple of m_memory
		if (dt > r_memory)
		{
			//int numRegionsToRemove = numberOfRegionsLoaded - maxRegionsLoadedAtOnce;
			//SortedSet sortedByAge = new TreeSet<Integer>(new Comparator(){});
			//if (numRegionsToRemove>0)
			for (int i=0;i<numberOfRegions;i++)
			{
				if (isRegionAvailable(i))
				{
					synchronized(regionAge)
					{
						regionAge[i] += dt;
					}
					
					if (regionAge[i]>r_memory)
					{
						System.out.printf("deleting a region %dms old (r_memory=%d)\n",regionAge[i],r_memory);
						
						// if it is unlocked, then remove it...
						if (regionLocks[i].tryLock())
						{
							try {
								unloadRegion(i);
								numberOfRegionsLoaded--;
							}
							finally {
								regionLocks[i].unlock();
							}
						}
						// else, ignore and try again next time...
						//else
						//	System.out.println("oops, I can't...");
					}
				}
			}		
		
			timeAtLastAgeUpdate += dt;		
		}
	}
	
	/// notifies the region master that this sample has more stuff to do 
	private synchronized void rescheduleSelf()
	{
		// make sure that only one instance of self is scheduled at once
		// otherwise we will get asynchronous access of the same audiofile, which we can't have
		if (!isScheduled)
		{
			isScheduled = true;
			regionMaster.execute(this);
		}
	}
}
