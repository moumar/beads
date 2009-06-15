/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;

/**
 * SamplePlayer plays back a {@link Sample}. Playback rate and loop points can be controlled by {@link UGen}s. The playback point in the {@link Sample} can also be directly controlled from {@link UGen} to perform scrubbing. The player can be set to a number of different loop modes. If constructed with a {@link Sample} argument, the number of outputs of SamplePlayer is determined by the number of channels of the {@link Sample}. {@link Sample} playback can use either linear or cubic interpolation.
 *
 * @author ollie
 */
public class SimpleSamplePlayer2 extends UGen {

	/**
	 * The Enum InterpolationType.
	 */
	public static enum InterpolationType {

		/** Use linear interpolation. */
		LINEAR, 

		/** Use cubic interpolation. */
		CUBIC
	};

	/** The Sample. */
	protected Sample buffer;            

	/** The sample rate, determined by the Sample. */
	protected float sampleRate;                 

	/** The position in milliseconds. */
	protected double position;                

	/** The millisecond position increment per sample. Calculated from the ratio of the {@link AudioContext}'s sample rate and the {@link Sample}'s sample rate. */
	protected double positionIncrement;           

	/** The interpolation type. */
	protected InterpolationType interpolationType;

	/** Flag to determine whether the SamplePlayer should kill itself when it gets to the end of the Sample. */
	protected boolean killOnEnd;

	/** The rate. Calculated and used internally from the rate envelope. */
	protected float rate;
	
	/** Array for temp storage. */
	protected float[] frame;

	/**
	 * Instantiates a new SamplePlayer with given number of outputs.
	 * 
	 * @param context the AudioContext.
	 * @param outs the number of outputs.
	 */
	public SimpleSamplePlayer2(AudioContext context, int outs) {
		super(context, outs);
		interpolationType = InterpolationType.LINEAR;
		killOnEnd = true;
		rate = 1f;
		position = 0f;
	}

	/**
	 * Instantiates a new SamplePlayer with given Sample. Number of outputs is determined by number of channels in Sample.
	 * 
	 * @param context the AudioContext.
	 * @param buffer the Sample.
	 */
	public SimpleSamplePlayer2(AudioContext context, Sample buffer) {
		this(context, buffer.getNumChannels());
		setBuffer(buffer);
	}

	/**
	 * Sets the Sample.
	 * 
	 * @param buffer the new Sample.
	 */
	public void setBuffer(Sample buffer) {
		this.buffer = buffer;
		sampleRate = buffer.getSampleRate();
		updatePositionIncrement();
		frame = new float[buffer.getNumChannels()];
	}

	/**
	 * Gets the Sample.
	 * 
	 * @return the Sample.
	 */
	public Sample getBuffer() {
		return buffer;
	}

	/**
	 * Sets the playback position to the end of the Sample.
	 */
	public void setToEnd() {
		position = buffer.getLength();
	}

	/**
	 * Starts the sample at the given position.
	 * 
	 * @param msPosition the position in milliseconds.
	 */
	public void start(float msPosition) {
		position = msPosition;
		start();
	}

	/**
	 * Resets the position to the start of the Sample.
	 */
	public void reset() {
		position = 0f;
	}

	/**
	 * Gets the playback position.
	 * 
	 * @return the position in milliseconds.
	 */
	public double getPosition() {
		return position;
	}

	/**
	 * Sets the playback position. This will not work if the position envelope is not null.
	 * 
	 * @param position the new position in milliseconds.
	 */
	public void setPosition(double position) {
		this.position = position;
	}
	
	public void setRate(float rate) {
		this.rate = rate;
	}

	/**
	 * Updates the position increment. Called whenever the {@link Sample}'s sample rate or the {@link AudioContext}'s sample rate is modified.
	 */
	private void updatePositionIncrement() {
		positionIncrement = context.samplesToMs(sampleRate / context.getSampleRate());
	}

	/**
	 * Gets the interpolation type.
	 * 
	 * @return the interpolation type.
	 */
	public InterpolationType getInterpolationType() {
		return interpolationType;
	}

	/**
	 * Sets the interpolation type.
	 * 
	 * @param interpolationType the new interpolation type.
	 */
	public void setInterpolationType(InterpolationType interpolationType) {
		this.interpolationType = interpolationType;
	}

	/**
	 * Gets the sample rate.
	 * 
	 * @return the sample rate, in samples per second.
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		if(buffer != null) {
			for (int i = 0; i < bufferSize; i++) {
				//calculate the samples		
				switch (interpolationType) {
				case LINEAR:
					buffer.getFrameLinear(position, frame);
					break;
				case CUBIC:
					buffer.getFrameCubic(position, frame);
					break;
				}
				for (int j = 0; j < outs; j++) {
					bufOut[j][i] = frame[j % buffer.getNumChannels()];
				}
				//update the position, loop state, direction
				calculateNextPosition(i);
				//if the SamplePlayer gets paused or deleted, zero the remaining outs and quit the loop
				if(isPaused() || isDeleted()) {
					//make sure to zero the remaining outs
					while(i < bufferSize) {
						for (int j = 0; j < outs; j++) {
							bufOut[j][i] = 0.0f;
						}
						i++;
					}
					break;
				}
			}
		}
	}

	/**
	 * Sets/unsets option for SamplePlayer to kill itself when it reaches the end of the Sample it is playing. True by default.
	 * 
	 * @param killOnEnd true to kill on end.
	 */
	public void setKillOnEnd(boolean killOnEnd) {
		this.killOnEnd = killOnEnd;
	}

	/**
	 * Determines whether this SamplePlayer will kill itself at the end of the Sample it is playing.
	 * 
	 * @return true of SamplePlayer will kill itself at the end of the Sample it is playing.
	 */
	public boolean getKillOnEnd() {
		return(killOnEnd);
	}

	/**
	 * Called when at the end of the Sample, assuming the loop mode is non-looping, or beginning, if the SamplePlayer is playing backwards..
	 */
	private void atEnd() {
		if (killOnEnd) {
			kill();
		}
		else {
			pause(true);
		}
	}

	/**
	 * Re trigger the SamplePlayer from the beginning.
	 */
	public void reTrigger() {
		reset();
		this.pause(false);
	}

	/**
	 * Used at each sample in the perform routine to determine the next playback position.
	 * 
	 * @param i the index within the buffer loop.
	 */
	protected void calculateNextPosition(int i) {
		position += positionIncrement * rate;
		if(position > buffer.getLength() || position < 0) atEnd();
	}


}
