package net.beadsproject.beads.data.sample;
import java.io.IOException;
import java.util.Arrays;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.data.AudioFile;
import net.beadsproject.beads.data.AudioFile.AudioFileUnsupportedException;

/**
 * A sample that loads an audio file immediately into memory. 
 * 
 * Creating a new BufferedSample will block until the entire audio file
 * is read into memory.
 * 
 * The audio file doesn't need to know it's length as BufferedSample just
 * keeps reading until there is no more data.
 * 
 * @author ben
 */
public class BufferedSample extends Sample {
	
	private float[][] f_sampleData;
		
	public BufferedSample(AudioContext ac, String filename) throws IOException, AudioFileUnsupportedException
	{
		super(ac);
		setFile(filename);
	}
	
	public BufferedSample(AudioContext ac, AudioFile audioFile) throws IOException, AudioFileUnsupportedException
	{
		super(ac);
		setFile(audioFile);
	}
	
	/**
	 * Specify an audio file that the Sample reads from.
	 */
	private void setFile(String file) throws IOException, AudioFileUnsupportedException
	{
		audioFile = getContext().getAudioIO().getAudioFile(file);
		setFile(audioFile);
	}
	
	/**
	 * Specify an explicit AudioFile that the Sample reads from.
	 * NOTE: Only one sample should reference a particular AudioFile.
	 * 
	 */
	private void setFile(AudioFile af) throws IOException, AudioFileUnsupportedException
	{
		audioFile = af;
		audioFile.open();
	
		audioFormat = audioFile.getFormat();
		nFrames = audioFile.getNumFrames();
		length = audioFile.getLength();

		loadAudioFileIntoSample();
	}
	
	@Override
	public void getFrame(int frame, float[] frameData)
	{
		if (frame<0 || frame >= getNumFrames())
		{
			for(int i=0;i<getNumChannels();i++)
				frameData[i] = 0;	
			return;
		}
		else
		{
			for(int i=0;i<getNumChannels();i++)
				frameData[i] = f_sampleData[i][frame];
		}
	}

	@Override
	public void getFrames(int frame, float[][] frameData) {
		if (frame >= getNumFrames())
		{
			for(int i=0;i<getNumChannels();i++)
				Arrays.fill(frameData[i],0);			
		}
		else
		{		
			int numFloats = Math.min(frameData[0].length,(int)(getNumFrames()-frame));			
			for(int i=0;i<getNumChannels();i++)
				System.arraycopy(f_sampleData[i],frame,frameData[i],0,numFloats);
		}
	}
	
	private void loadAudioFileIntoSample() throws IOException
	{
		System.err.println("Loading...");
		
		if (audioFile.getFrameSize() != 2*audioFile.getNumChannels())
		{
			System.err.printf("File has an unsupported depth of %d-bit\n", 8*(audioFile.getFrameSize()/getNumChannels()));
			audioFile.close();
			f_sampleData = null;
			return;
			
			// Later, we can support this with the following...
			// AudioUtils.byteToFloatWithNBytesPerFloat(interleaved, data, frameSize/getNumChannels(), getAudioFormat().isBigEndian(),0,0,interleaved.length);
		};
		
		final int BUFFERSIZE = 4096;
		byte[] audioBytes = new byte[BUFFERSIZE];
	
		int sampleBufferSize = 4096;
		byte[] data = new byte[sampleBufferSize];
	
		int bytesRead;
		int totalBytesRead = 0;	
		int numberOfFrames = 0;
		
		int frameSize = audioFile.getFrameSize();
	
		while ((bytesRead = audioFile.read(audioBytes))!=-1) {	
			int numFramesJustRead = bytesRead / frameSize; // (2 * getNumChannels());
	
			if (bytesRead > (sampleBufferSize-totalBytesRead))
			{
				sampleBufferSize = Math.max(sampleBufferSize*2, sampleBufferSize + bytesRead);
				byte[] newBuf = new byte[sampleBufferSize];
				System.arraycopy(data, 0, newBuf, 0, data.length);					
				data = newBuf;
			}
	
			System.arraycopy(audioBytes, 0, data, totalBytesRead, bytesRead);			
	
			numberOfFrames += numFramesJustRead;
			totalBytesRead += bytesRead;
		}
	
		// resize buf to proper length, if necessary
		if (sampleBufferSize > totalBytesRead)
		{
			sampleBufferSize = totalBytesRead;
	
			// resize buffer				
			byte[] newBuf = new byte[sampleBufferSize];
			System.arraycopy(data, 0, newBuf, 0, sampleBufferSize);					
			data = newBuf;
		}
	
		this.nFrames = sampleBufferSize / frameSize; // (2*getNumChannels());
		this.length = 1000f * nFrames / audioFormat.getSampleRate();
	
		// copy and deinterleave entire data	
		f_sampleData = new float[getNumChannels()][(int) nFrames];
		float[] interleaved = new float[(int) (getNumChannels()*nFrames)];
		AudioUtils.byteToFloat(interleaved, data, getAudioFormat().isBigEndian());
		AudioUtils.deinterleave(interleaved, getNumChannels(), (int) nFrames, f_sampleData);
		
		audioFile.close();		
	}

}
