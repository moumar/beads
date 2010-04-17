package net.beadsproject.beads.data.sample;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioFormat;
import net.beadsproject.beads.data.AudioFile;

/**
 * Abstract Sample class. Under construction.
 * 
 *  
 * @author ben
 * 
 * TODO: Support for 24-bit files
 */
public abstract class Sample {
	protected AudioContext audioContext;
	
	protected AudioFile audioFile = null;
	
	protected AudioFormat audioFormat;	
	protected long nFrames;	
	protected float length; // length in ms, derived from nFrames
	
	private float[] _current = null, _next = null; //used as temp buffers whilst calculating interpolation
	
	public Sample(AudioContext ac)
	{
		audioContext = ac;
	}	
	
	/**
	 * Return a single frame. 
	 *  
	 * @param frame Must be in range and available, else frameData is zero-filled.  
	 * @param frameData
	 * 
	 */
	public abstract void getFrame(int frame, float[] frameData);	

	/**
	 * Get a series of frames. FrameData will only be filled with the available frames. 
	 * It is the caller's responsibility to count how many frames are valid.
	 * <code>min(nFrames - frame, frameData[0].length)</code> frames in frameData are valid.  
	 * 
	 * If the data is not readily available this doesn't do anything.
	 * 
	 * @param frame The frame number
	 * @param frameData
	 */
	public abstract void getFrames(int frame, float[][] frameData);
	
	
	
	
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public int getNumChannels() {
		return audioFormat.getChannels();
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
	
	public AudioContext getContext()
	{
		return audioContext;
	}
	
	/**
	 * Prints audio format info to System.out.
	 */
	public void printAudioFormatInfo() {
		System.out.println("Sample Rate: " + audioFormat.getSampleRate());
		System.out.println("Channels: " + getNumFrames());
		System.out.println("Frame size in Bytes: " + audioFormat.getFrameSize());
		System.out.println("Encoding: " + audioFormat.getEncoding());
		System.out.println("Big Endian: " + audioFormat.isBigEndian());
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
	 * Retrieves a frame of audio using no interpolation. 
	 * If the frame is not in the sample range then zeros are returned.
	 * 
	 * @param posInMS The frame to read -- will take the last frame before this one.
	 * @param result The framedata to fill.
	 */
	public void getFrameNoInterp(double posInMS, float[] result)
	{
		double frame = msToSamples(posInMS);
		int frame_floor = (int)Math.floor(frame);
		getFrame(frame_floor,result);		
	}
	
	/**
	 * Retrieves a frame of audio using linear interpolation. 
	 * If the frame is not in the sample range then zeros are returned.
	 * 
	 * @param posInMS The frame to read -- can be fractional (e.g., 4.4).
	 * @param result The framedata to fill.
	 */
	public void getFrameLinear(double posInMS, float[] result) {
		if (_current==null)
		{
			_current = new float[getNumChannels()];
			_next = new float[getNumChannels()];
		}
		
		double frame = msToSamples(posInMS);
		int frame_floor = (int)Math.floor(frame);
		if(frame_floor > 0 && frame_floor < nFrames) {			
			double frame_frac = frame - frame_floor;
			if (frame_floor==nFrames-1)
			{
				getFrame(frame_floor,result);
			}
			else // lerp
			{
				getFrame(frame_floor,_current);
				getFrame(frame_floor+1,_next);
				for (int i = 0; i < getNumChannels(); i++) {
					result[i] = (float)((1 - frame_frac) * _current[i] + frame_frac * _next[i]);
				} 
			}
		} else {
			for(int i = 0; i < getNumChannels(); i++) {
				result[i] = 0.0f;
			}
		}
	}
	
	/**
	 * Retrieves a frame of audio using cubic interpolation.
	 * If the frame is not in the sample range then zeros are returned.
	 * 
	 * @param posInMS The frame to read -- can be fractional (e.g., 4.4).
	 * @param result The framedata to fill.
	 */
	public void getFrameCubic(double posInMS, float[] result) {
		if (_current==null)
		{
			_current = new float[getNumChannels()];
			_next = new float[getNumChannels()];
		}
		
		double frame = msToSamples(posInMS);
		float a0,a1,a2,a3,mu2;
		float ym1,y0,y1,y2;
		for (int i = 0; i < getNumChannels(); i++) {
			int realCurrentSample = (int)Math.floor(frame);
			float fractionOffset = (float)(frame - realCurrentSample);
			
			if(realCurrentSample >= 0 && realCurrentSample < (nFrames - 1)) {
				realCurrentSample--;
				if (realCurrentSample < 0) {
					getFrame(0, _current);
					ym1 = _current[i];
					realCurrentSample = 0;
				} else {
					getFrame(realCurrentSample++, _current);
					ym1 = _current[i];
				}
				getFrame(realCurrentSample++, _current);
				y0 = _current[i];
				if (realCurrentSample >= nFrames) {
					getFrame((int)nFrames - 1, _current);
					y1 = _current[i]; //??
				} else {
					getFrame(realCurrentSample++, _current);
					y1 = _current[i];
				}
				if (realCurrentSample >= nFrames) {
					getFrame((int)nFrames - 1, _current);
					y2 = _current[i]; //??
				} else {
					getFrame(realCurrentSample++, _current);
					y2 = _current[i];
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
	}

	public boolean hasAudioFile()
	{
		return audioFile!=null;
	}
	
	public AudioFile getAudioFile()
	{
		return audioFile;
	}
}
