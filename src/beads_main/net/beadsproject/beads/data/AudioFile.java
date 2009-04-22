package net.beadsproject.beads.data;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.beadsproject.beads.core.AudioUtils;

/**
 * Represents an audio file. Handles loading and format conversion. 
 * 
 * A sample can be made to wrap an AudioFile and provide intelligent buffered access to the data.  
 * TODO: Intelligently approximate the length of the audio stream..?
 * 
 * @author ben
 */
public class AudioFile {
	public File file;
	public AudioFileFormat audioFileFormat;

	/** The number of channels. */
	public int nChannels;

	/** The total number of frames.
	 *  nFrames = -1 means that we don't know when this stream will end. 
	 *  Also see: isFinite
	 **/
	public long nFrames;
	public long nTotalFramesRead = 0;
	public boolean finished = false;

	// stream-specific stuff
	private AudioFormat encodedFormat;
	private AudioFormat decodedFormat;
	private AudioInputStream encodedStream = null;
	private AudioInputStream decodedStream = null;	
	private int numBytes = 2; // number of bytes per frame per channel (e.g., numBytes=2 for a 16-bit audio file)	
	private boolean isEncoded = false; // is the audio file encoded 
	private int bufferSize;

	/**
	 * Load an audio file from disk. The audiofile needs to be open()'ed before it's data can be read.
	 * Note: AudioFile provides low-level access to audio files -- If you just want to access the data of a sound file use a Sample.
	 * @see Sample 
	 * 
	 * @param filename
	 *
	 * @throws IOException If the file cannot be found or opened.  
	 * @throws UnsupportedAudioFileException If the file is of an unsupported audio type.
	 */
	public AudioFile(String filename) throws IOException, UnsupportedAudioFileException {
		// store a maximum of 10 meg 
		this(filename,1024*1024*10);
	}

	/**
	 * Advanced: Create an input stream from a file, but don't keep more than numBytes of data in memory.
	 * 
	 * @param filename
	 * @param bufferSize The maximum number of bytes the audiofile can keep in memory.   
	 *
	 * @throws IOException If the file cannot be found or opened.  
	 * @throws UnsupportedAudioFileException If the file is of an unsupported audio type.
	 */
	public AudioFile(String filename, int bufferSize) throws IOException, UnsupportedAudioFileException {
		file = new File(filename);						
		audioFileFormat = AudioSystem.getAudioFileFormat(file);
		nFrames = audioFileFormat.getFrameLength();
		this.bufferSize = bufferSize;
		nTotalFramesRead = 0;
	}

	/**
	 * Reset the audio input stream. For some audio formats, this may involve re-opening the associated file.
	 */
	public void reset()
	{
		try{
			if (encodedStream.markSupported())		
			{
				try{
					encodedStream.reset();					
					nTotalFramesRead = 0;
				}
				catch(IOException e)
				{
					close();
					open();
				}			
			}
			else
			{
				close();			
				open();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Skips a number of frames.
	 * Note: this function skips frames, not bytes.
	 * Doesn't work for vbr!
	 * 
	 * @param frames
	 */
	public void skip(long frames)
	{
		try {
			if (isEncoded)
			{
				/* GAH, DOESN'T WORK!
				int framesizebytes = Integer.decode(audioFileFormat.properties().get("mp3.framesize.bytes").toString());
				System.out.println(framesizebytes);				
				long numbytes = frames * framesizebytes;
				encodedStream.skip(numbytes);
				*/
				System.out.println("FIX THE SKIP METHOD FOR ENCODED STREAMS!");
				System.exit(1);
			}
			else
				decodedStream.skip(nChannels*numBytes*frames);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nTotalFramesRead += frames;
	}

	/**
	 * Seek to a specific frame number. Note that seeking is slower than skipping forward.
	 * 
	 * @param frame The frame number, relative to the start of the audio data.
	 */
	public void seek(int frame)
	{
		reset();
		skip(frame);
	}

	/**
	 * Opens the audio file, ready for data access.
	 * 
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public void open() throws UnsupportedAudioFileException, IOException
	{
		// TODO: Implement reset() to takes some shortcuts - rather than executing all of the logic below

		if (file.exists()) encodedStream = AudioSystem.getAudioInputStream(file);
		encodedFormat = encodedStream.getFormat();
		decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				encodedFormat.getSampleRate(),
				16,
				encodedFormat.getChannels(),
				encodedFormat.getChannels()*2, // 2*8 = 16-bits per sample per channel
				44100,
				encodedFormat.isBigEndian());

		if (AudioSystem.isConversionSupported(decodedFormat, encodedFormat))
		{
			isEncoded = true;
			decodedStream = AudioSystem.getAudioInputStream(decodedFormat, encodedStream);
			nFrames = AudioSystem.NOT_SPECIFIED;

			Map<String,Object> properties = audioFileFormat.properties();
			if (properties.containsKey("duration"))
			{
				long mslength = Long.decode(properties.get("duration").toString()) / 1000;
				// convert to samples
				nFrames = (long)((decodedFormat.getSampleRate()/1000) * mslength);				
			}
		}
		else
		{
			// try to use the undecoded format
			isEncoded = false;
			decodedFormat = encodedFormat;
			decodedStream = encodedStream;

			nFrames = (int)(decodedStream.getFrameLength());
		}

		numBytes = decodedFormat.getSampleSizeInBits()/8;
		nChannels = decodedFormat.getChannels();

		nTotalFramesRead = 0;

		if (nFrames==AudioSystem.NOT_SPECIFIED)
		{
			System.out.println("Cannot determine the length of the audio file: " + file.getName());
			System.out.println("AudioFile needs to know the length in order to operate appropriately.");
			System.out.println("Now exiting.");
			System.exit(1);
		}

		if (encodedStream.markSupported())
			encodedStream.mark(Math.min(bufferSize,(int) file.length()));
	}

	/**
	 * Close the audio file. Can be re-opened.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		if (isEncoded)
			decodedStream.close();
		encodedStream.close();
		encodedStream = null;
		decodedStream = null;		
	}

	/**
	 * Is the file stream open?
	 */
	public boolean isOpen()
	{
		return !isClosed();
	}

	/**
	 * Is the file stream closed?
	 */
	public boolean isClosed()
	{
		return encodedStream==null;
	}

	/**
	 * @return The number of bytes per frame per channel.
	 */
	public int byteDepth()
	{
		return this.numBytes;
	}

	public AudioFormat getDecodedFormat()
	{
		return decodedFormat;
	}

	public AudioFormat getEncodedFormat() 
	{
		return encodedFormat;
	}

	/**
	 * Read bytes directly from the decoded audiofile.
	 * The bytes will be in an interleaved format. It is the responsibility of the caller to interpret this data correctly.  
	 *  
	 * The number of bytes read is equal to the size of the byte buffer. 
	 * If that many bytes aren't available the buffer will only be partially filled.   
	 * 
	 * @param buffer A buffer to fill.
	 * 
	 * @return The number of bytes read. A value of -1 indicates the file has no data left.
	 */
	public int read(byte[] buffer) {
		if (finished) return -1;

		// read the next bufferSize frames from the input stream		
		int actualBytesRead = -1;
		try {
			actualBytesRead = decodedStream.read(buffer,0,buffer.length);
		} catch (IOException e) {
			finished = true;
		}

		if (finished || actualBytesRead==-1)
		{
			finished = true;
			return -1;
		}

		nTotalFramesRead += actualBytesRead / (2*nChannels);
		return actualBytesRead;
	}

	/**
	 * Read decoded audio data in a non-interleaved, Beads-friendly format.
	 * 
	 * Note: This function is <b>extremely inefficient</b> if the buffer size is constant. Use Sample, it is very efficient!
	 * 
	 * @param buffer The buffer to fill. After execution buffer[i][j] will contain the sample in channel i, frame j. Buffer has size (numChannels,numFramesRequested). 
	 * 
	 * @return The number of <u>frames</u> read.
	 */
	public int read(float[][] buffer)
	{	
		// TODO: Make this more efficient, too many memory allocations
		if (buffer.length!=nChannels || buffer[0].length==0) return 0;			
		else if (finished) return 0;
		// else, read the data

		// read the next bufferSize frames from the input stream		
		byte[] byteBuffer = new byte[buffer[0].length * nChannels * numBytes];
		int actualBytesRead = -1;
		try {
			actualBytesRead = decodedStream.read(byteBuffer,0,byteBuffer.length);
		} catch (IOException e) {
			finished = true;
		}

		if (finished || actualBytesRead==-1)
		{
			finished = true;
			return 0;
		}

		int numFramesJustRead = actualBytesRead / (2*nChannels); 
		nTotalFramesRead += numFramesJustRead;	

		float[] floatbuf = new float[buffer[0].length*nChannels*numFramesJustRead];
		AudioUtils.byteToFloat(floatbuf, byteBuffer, decodedFormat.isBigEndian(),numFramesJustRead*nChannels);    		
		AudioUtils.deinterleave(floatbuf,nChannels,numFramesJustRead,buffer);

		return numFramesJustRead;
	}
}

