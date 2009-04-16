package net.beadsproject.beads.ugens;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;

/**
 * A StreamingAudioIn is a UGen that can stream audio files (.wav,.aif,.mp3).
 * Alternatively a StreamingAudioIn can be used to access the decoded audio data from a stream.
 *  
 * TODO: Add reset() functionality.
 * @author ben
 */
public class StreamingAudioIn extends UGen{
	/** The corresponding file of this stream. */
	public String fileName;

	/** The audio format of the undecoded and decoded streams. */
	public AudioFormat undecodedFormat;
	public AudioFormat decodedFormat;    

	/** The number of channels. */
	public final int nChannels;

	/** The total number of frames.
	 *  nFrames = -1 means that we don't know when this stream will end. 
	 *  Also see: isFinite
	 **/
	public final int nFrames;
	public int nTotalFramesRead = 0;

	/**
	 * A stream is finite if it will end eventually. 
	 * An infinite stream (like real-time input from a microphone) will go on forever, until destroyed. 
	 */
	public final boolean isFinite;

	/** finished=True if the audioinputstream has no more data */
	public boolean finished = false;

	// stream-specific stuff
	private javax.sound.sampled.AudioInputStream stream;
	private int byteBufferSize;
	private byte[] byteBuffer;    
	private float[] floatBuffer;

	/**
	 * Create an input stream from a file.
	 * @param context
	 * @param filename
	 * @throws IOException 
	 *
	 * TODO: Support more channels?
	 */
	public StreamingAudioIn(AudioContext context, String filename) throws IOException {
		super(context,0,2); // assume 2 channels

		this.fileName = filename;	
		this.isFinite = true; // files are always finite

		javax.sound.sampled.AudioInputStream audioInputStream = null;
		try {
			File fileIn = new File(this.fileName);
			if (fileIn.exists())
				audioInputStream = AudioSystem.getAudioInputStream(fileIn);
			else
				audioInputStream = AudioSystem.getAudioInputStream((new URL(this.fileName)).openStream());
		} catch(Exception e) {
			throw(new IOException("Cannot find file \"" + this.fileName + "\". It either doesn't exist at the specified location or the URL is malformed."));    		    		
		}
		undecodedFormat = audioInputStream.getFormat();
		nChannels = undecodedFormat.getChannels();
		nFrames = (int)audioInputStream.getFrameLength();

		AudioFormat acf = context.getAudioFormat();

		decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				acf.getSampleRate(),
				16,
				acf.getChannels(),
				acf.getChannels()*2, // 2*8 = 16-bits per sample per channel
				acf.getSampleRate(),
				undecodedFormat.isBigEndian());  

		if (AudioSystem.isConversionSupported(decodedFormat, undecodedFormat))
		{
			// System.out.println("Target Format : "+decodedFormat.toString());
			stream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream);				
		}
		else
		{ 
			// try to use the undecoded format
			decodedFormat = undecodedFormat;
			stream = audioInputStream;
			// throw(new IOException("Cannot decode the stream " + this.fileName));
		}
		
		// calculate the bytebuffersize
		byteBufferSize = this.bufferSize*nChannels*2; // 16-bit * numFrames * numChannels
		byteBuffer = new byte[byteBufferSize];
		floatBuffer = new float[this.bufferSize*nChannels];
	}

	@Override
	public void calculateBuffer() {
		if (finished) return;

		// read the next bufferSize frames from the input stream		
		int actualBytesRead = -1;
		try {
			actualBytesRead = stream.read(byteBuffer,0,byteBufferSize);
		} catch (IOException e) {
			finished = true;			
		}

		if (finished || actualBytesRead==-1)
		{
			finished = true;
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int ch=0;ch<nChannels;ch++)
				Arrays.fill(bufOut[ch],0);
			return;
		}
		int numFramesJustRead = actualBytesRead / (2*nChannels);
		AudioUtils.byteToFloat(floatBuffer, byteBuffer, decodedFormat.isBigEndian(),numFramesJustRead*nChannels);    		
		AudioUtils.deinterleave(floatBuffer,nChannels,numFramesJustRead,bufOut);
		if (numFramesJustRead < bufferSize)
		{
			// fill the end of the buffer with silence
			for(float[] b: bufOut)	
				Arrays.fill(b,numFramesJustRead,bufferSize,0);			
		}
		nTotalFramesRead += numFramesJustRead;
	}
	
	/**
	 * This function creates a sample by streaming continuously into it until the stream is empty.
	 * IMPORTANT: Do not use this on very large or infinite streams as you may find this blocking (possibly) forever.
	 * If you more flexible recording functionality, use Recorder instead.
	 * 
	 * Note: This will drain the stream, and hence must be reset if you intend to use it again.
	 * @return
	 * @throws Exception 
	 */
	public Sample createSample() throws Exception
	{
		if (finished || stream==null) throw new Exception("Can't create sample from an empty stream.");
		
		final int BUFFERSIZE = 4096;
        byte[] audioBytes = new byte[BUFFERSIZE];
        
        int sampleBufferSize = 4096;
        float[][] buf = new float[nChannels][sampleBufferSize];
        int bytesRead;
        int nFramesReadThisTime = 0;
		while ((bytesRead = stream.read(audioBytes,0,BUFFERSIZE)) != -1) {
			int numFramesJustRead = (bytesRead / (2*nChannels));
			float[] bufTemp = new float[nChannels * numFramesJustRead];
			// resize buf if necessary
			if (numFramesJustRead > (sampleBufferSize-nFramesReadThisTime))
			{
				sampleBufferSize = Math.max(sampleBufferSize*2, sampleBufferSize + numFramesJustRead);
				System.out.printf("Adjusted samplebuffersize to %d\n",sampleBufferSize);
				for(int i=0;i<nChannels;i++) {
					int length = buf[i].length;
					buf[i] = new float[sampleBufferSize];
					length = Math.min(length, sampleBufferSize);
					for(int j = 0; j < length; j++) {
							buf[i][j] = buf[i][j];
					}
				}
			}
			AudioUtils.byteToFloat(bufTemp, audioBytes, decodedFormat.isBigEndian(),numFramesJustRead*nChannels);    		
			//float[][] bufSegment = new float[nChannels][numFramesJustRead]; 
			//AudioUtils.deinterleave(bufTemp,nChannels,numFramesJustRead,bufSegment);
			
			float[][] bufSegment = deinterleave(bufTemp,nChannels);
			for (int i = 0; i < bufSegment.length; i++) {
				for (int j = 0; j < bufSegment[i].length; j++) {
					buf[i][j + nFramesReadThisTime] = bufSegment[i][j];
				}
			}
			nTotalFramesRead += numFramesJustRead;
			nFramesReadThisTime += numFramesJustRead;
        }
		// resize buf to proper length
		// resize buf if necessary
		if (sampleBufferSize > nFramesReadThisTime)
		{
			sampleBufferSize = nFramesReadThisTime;
			for(int i=0;i<nChannels;i++) {
				int length = buf[i].length;
				buf[i] = new float[sampleBufferSize];
				length = Math.min(length, sampleBufferSize);
				for(int j = 0; j < length; j++) {
						buf[i][j] = buf[i][j];
				}
			}
		}
		
        stream.close();
        finished = true;
        
        Sample s = new Sample(decodedFormat,nFramesReadThisTime);
        s.fileName = this.fileName;
        for(int i=0;i<nChannels;i++)
        	System.arraycopy(buf[i],0,s.buf[i],0,nFramesReadThisTime);
        return s;       
	}
	
	private static float[][] deinterleave(float[] source, int nChannels) {
    	int nFrames = source.length / nChannels;
        float[][] result = new float[nChannels][nFrames];   
        for(int i = 0, count = 0; i < nFrames; i++) {
            for(int j = 0; j < nChannels; j++) {
                result[j][i] = source[count++];
            }
        }
        return result;
    }

}