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
 * Similar to {@link BufferedSample} but only loads data when needed. 
 * Once loaded the data remains in memory. 
 * 
 * TODO: Problem when looping small file
 * 
 * @author ben
 */
public class StreamingSample extends Sample{

	private float[][][] f_regions; // uninterleaved data
	private int numberOfRegions; // total number of regions
	private int numberOfRegionsLoaded; // number of loaded regions
	
	private int regionSizeInFrames; // region size in frames
	private int lookaheadInRegions; // num region lookahead
	private int regionSizeInBytes; // the number of bytes per region (regionSize * getNumChannels() * bitconversionfactor)
		
	private float msToLoadAhead; // amount of audio data to load ahead
	
	public StreamingSample(AudioContext ac, String filename, float msToLoadAhead) throws IOException, AudioFileUnsupportedException
	{   
		super(ac);
		
		this.msToLoadAhead = msToLoadAhead;
		this.numberOfRegionsLoaded = 0;
		
		setFile(filename);
	}
	
	public StreamingSample(AudioContext ac, String filename) throws IOException, AudioFileUnsupportedException
	{   
		this(ac,filename,1000);
	}
		
	/**
	 * Return a single frame. 
	 * 
	 * If the data is not available this doesn't do anything to frameData.
	 *  
	 * @param frame Must be in range, else framedata is unchanged. 
	 * @param frameData
	 * 
	 */
	public void getFrame(int frame, float[] frameData)
	{
		int whichRegion = frame / regionSizeInFrames;
		if (whichRegion > numberOfRegions || frame < 0 || frame>=nFrames)
		{
			Arrays.fill(frameData,0.f);		
			return;
		}			
						
		float[][] regionData = getRegionF(whichRegion);
		if (regionData!=null)
		{
			int startIndex = frame % regionSizeInFrames;
			for(int i=0;i<getNumChannels();i++)
				frameData[i] = regionData[i][startIndex];
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
		int whichregion = frame / regionSizeInFrames;
		if (whichregion > numberOfRegions || frame >= nFrames)
		{
			for(int i=0;i<getNumChannels();i++)
			{
				Arrays.fill(frameData[i],0.f);
			}
			return;
		}
		
		int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame));			
		int floatdataindex = 0;
		int regionindex = frame % regionSizeInFrames;
		int numfloatstocopy = Math.min(regionSizeInFrames - regionindex, numFloats - floatdataindex);

		while (numfloatstocopy>0)
		{
			float[][] regionData = getRegionF(whichregion);
			for(int i=0;i<getNumChannels();i++)
				System.arraycopy(regionData[i], 0, frameData[i], floatdataindex, numfloatstocopy);			
			floatdataindex += numfloatstocopy;
			regionindex = 0;				
			numfloatstocopy = Math.min(regionSizeInFrames, numFloats - floatdataindex);				
			whichregion++;			
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
		
		if (audioFormat.getFrameSize() != audioFormat.getChannels()*2)
			throw(new AudioFileUnsupportedException("Can't load non 16-bit files, yet."));
	
		init();
	}
	
	/// set everything up, ready to use
	private void init() throws IOException, AudioFileUnsupportedException
	{		
		if (nFrames==-1)
		{
			// TODO: Do a quick run through and guess the length?
			throw(new AudioFile.AudioFileUnsupportedException("BufferedOnDemandSample cannot determine the length of the audio file for buffering. \n" +
					"Use a BufferedSample instead."));					
		}		
		else
		{				
			regionSizeInFrames = (int)Math.ceil(((msToLoadAhead/1000.) * audioFile.getFormat().getSampleRate()));
			if (regionSizeInFrames>nFrames)
			{
				regionSizeInFrames = (int) nFrames;
				lookaheadInRegions = 0;
				numberOfRegions = 1;
			}
			else
			{
				lookaheadInRegions = 2;
				numberOfRegions = 1 + (int) ((float)nFrames / regionSizeInFrames);
			}
			
			regionSizeInBytes = regionSizeInFrames * 2 * getNumChannels(); // 16-bit
			numberOfRegionsLoaded = 0;
			
			f_regions = new float[numberOfRegions][][];
			Arrays.fill(f_regions,null);				
		}
		
	}	
	
	/// Region handling, loading, etc...
	private float[][] getRegionF(int r)
	{	
		if (!isRegionAvailable(r))
		{
			loadRegion(r+lookaheadInRegions);
		}
		
		return f_regions[r];
	}
	
	public boolean isRegionAvailable(int r)
	{
		return f_regions[r]!=null;
	}
		
	/** 
	 * Loads all regions up to and including region r
	 * @param r
	 */
	private void loadRegion(int r)
	{			
		if (r < numberOfRegionsLoaded)
		{
			return;
		}
		
		int numberOfRegionsToLoad = Math.min(numberOfRegions - numberOfRegionsLoaded, 1 + r - numberOfRegionsLoaded);
		if (numberOfRegionsToLoad<=0) return;
		
		byte[] region = new byte[regionSizeInBytes*numberOfRegionsToLoad];
		int bytesRead = audioFile.read(region);
		int actualRegionsLoaded = (int) Math.ceil((double)bytesRead / regionSizeInBytes);
		
		r = numberOfRegionsLoaded + actualRegionsLoaded - 1;
		
		if (bytesRead<=0)
		{
			for(int i=numberOfRegionsLoaded; i<=r; i++)
			{
				f_regions[r] = new float[getNumChannels()][regionSizeInFrames];
				for(int j=0;j<getNumChannels();j++)
					Arrays.fill(f_regions[r][j],0.f);
			}
			numberOfRegionsLoaded = 1+r;
		}
		else
		{	
			// convert the byte data to frames
			// and handle the special case of a partial frame
			int numFullRegionsLoaded = bytesRead / regionSizeInBytes;			
			
			float[] interleaved = new float[getNumChannels()*regionSizeInFrames];
			for(int k=0;k<numFullRegionsLoaded;k++)
			{	
				int i = numberOfRegionsLoaded + k;
				f_regions[i] = new float[getNumChannels()][regionSizeInFrames];	
				AudioUtils.byteToFloat(interleaved, region, getAudioFormat().isBigEndian(), regionSizeInBytes*k, interleaved.length);	
				AudioUtils.deinterleave(interleaved, getNumChannels(), regionSizeInFrames, f_regions[i]);
			}
			
			if (numFullRegionsLoaded!=numberOfRegionsToLoad)
			{
				int lastRegionSizeInBytes = bytesRead - regionSizeInBytes*numFullRegionsLoaded;
				if (lastRegionSizeInBytes < regionSizeInBytes)					
				{
					// we have a partial frame
					int f = numFullRegionsLoaded;
					int nf = lastRegionSizeInBytes/(2*getNumChannels());					
					f_regions[f] = new float[getNumChannels()][regionSizeInFrames];
					
					// load the partial data, and fill the remainder with silence
					AudioUtils.byteToFloat(interleaved, region, getAudioFormat().isBigEndian(), regionSizeInBytes*f, nf);
					for(int i=nf; i<interleaved.length; i++)
						interleaved[i] = 0;					
					AudioUtils.deinterleave(interleaved, getNumChannels(), regionSizeInFrames, f_regions[f]);
					
					numberOfRegionsLoaded += (numFullRegionsLoaded+1);		
				}
				else
				{
					System.err.printf("An error has occurred.");
				}
			}
			else			
				numberOfRegionsLoaded += numFullRegionsLoaded;
		}		
	}
}
