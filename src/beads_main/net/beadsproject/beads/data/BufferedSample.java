package net.beadsproject.beads.data;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.beadsproject.beads.core.AudioUtils;

/**
 * A Buffered Sample provides random access to an audio file. 
 * 
 * Only supports 16-bit samples at the moment.
 * Ben's note: Interpolation should be handled by the sample player. 
 *    
 * @author ben
 */
public class BufferedSample {

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
	static private final int regionSize = 512;    
	private int regionSizeInBytes; // the number of bytes per region (regionSize * nChannels * bitconversionfactor)
	
	private int lookahead = 5; // num region lookahead
	private int lookback = 0; // num regions lookback
	private int maxRegionsLoadedAtOnce; // when memory conservation is more important than run-time performance
	private int bufferMax = 1;
	private int numberOfRegions;
	private byte[][] regions;

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
	 * BufferingRegime.TIMED: Only some parts of the audio file are stored in memory. Various parameters affect the buffering behaviour.  
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
		this.lookahead = lookahead;
	}

	//set how many milliseconds  from last loaded  point to look back
	public void setLookBack(int lookback) 
	{
		this.lookback = lookback;
	}

	/**
	 * If a part of an audio file has not been accessed for some amount of time it is discarded.
	 * 
	 * @param ms Duration in milliseconds that unaccessed regions remain loaded.  
	 */
	public void setMemory(float ms)
	{
		// TODO: implement this
	}

	//set the max space used in MB
	/**
	 * Specify the maximum amount of memory this sample uses.
	 * If the sample is large this helps with conserving ram.
	 * 
	 * @param mb Size of buffer (in megabytes)
	 */
	public void setBufferMax(int mb)
	{
		this.bufferMax = mb;
	}

	/*
	 * Specify an audio file that the Sample reads from.
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
		}
		else if (bufferingRegime==BufferingRegime.TIMED)
		{
			if (nFrames==AudioSystem.NOT_SPECIFIED)
			{
				// TODO: Do a quick run through and guess the length?
				System.out.println("BufferedSample needs to know the length of the audio file it uses, but cannot determine the length.");
				System.exit(1);
			}
			
			numberOfRegions = 1 + (int)(nFrames / regionSize);
			regions = new byte[numberOfRegions][];
			Arrays.fill(regions,null);
			
			regionSizeInBytes = regionSize * 2 * nChannels;
			
			// load first N regions ... just for testing...
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
		}
	}

	/**
	 * Return a single frame.
	 * TODO: What should an invalid frame request return?
	 *  
	 * @param frame Must be in range. Else framedata is unchanged. 
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
			byte[] regionData = getRegion(whichRegion);
			
			// convert it to the correct format,
			int startIndex = (frame % regionSize) * 2 * nChannels;
			AudioUtils.byteToFloat(frameData,regionData,audioFile.getDecodedFormat().isBigEndian(),startIndex,frameData.length);			
		}
	}

	/**
	 * Get a series of frames. FrameData will only be filled with the available frames. 
	 * It is the caller's responsibility to count how many frames are valid.
	 * e.g., min(nFrames - frame, frameData[0].length) frames in frameData are valid.  
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
				byte[] regionData = getRegion(whichregion);
				AudioUtils.byteToFloat(floatdata, regionData, audioFile.getDecodedFormat().isBigEndian(), regionindex*2*nChannels, floatdataindex*nChannels, numfloatstocopy*nChannels);
				
				floatdataindex += numfloatstocopy;
				regionindex = 0;				
				numfloatstocopy = Math.min(regionSize,numFloats - floatdataindex);				
				whichregion++;
			}
			
			// deinterleave the whole thing			
			AudioUtils.deinterleave(floatdata,nChannels,frameData[0].length,frameData);
		}
	}
	
	
	/// Region handling, loading, etc...
	// assume region r exists
	private byte[] getRegion(int r) 
	{
		// first determine whether the region is valid
		if (regions[r]==null)
		{
			// this is bad, load the region immediately
			loadRegion(r);			
		}
		
		return regions[r];
	}
	
	private void loadRegion(int r)
	{
		// for now, just seek to the correct position 
		try {
			audioFile.seek(regionSize*r);
			regions[r] = new byte[regionSizeInBytes];
			audioFile.read(regions[r]);			
		} catch (Exception  e) {
			e.printStackTrace();
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
				System.out.printf("Adjusted samplebuffersize to %d\n",sampleBufferSize);
				
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
