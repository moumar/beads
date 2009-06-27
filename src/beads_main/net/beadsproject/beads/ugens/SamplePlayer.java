/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;

/*
 *  TODO: Sample Player has some redundancy when using using constant (or even linear) rate envelopes. For 
 *  example getFrame() is called twice as much as it should be when the rate is below one.
 *  
 */

/**
 * SamplePlayer plays back a {@link Sample}. Playback rate and loop points can be controlled by {@link UGen}s. The playback point in the {@link Sample} can also be directly controlled from {@link UGen} to perform scrubbing. The player can be set to a number of different loop modes. If constructed with a {@link Sample} argument, the number of outputs of SamplePlayer is determined by the number of channels of the {@link Sample}. {@link Sample} playback can use either linear or cubic interpolation.
 * 
 * @beads.category sample players
 * @author ollie
 */
public class SamplePlayer extends UGen {

	public static final float ADAPTIVE_INTERP_LOW_THRESH = 0.5f;
	public static final float ADAPTIVE_INTERP_HIGH_THRESH = 2.5f;
	
	/**
	 * The Enum InterpolationType.
	 */
	public static enum InterpolationType {

		/** Use no interpolation. */
		NONE, 
		
		/** Use linear interpolation. */
		LINEAR, 

		/** Use cubic interpolation. */
		CUBIC,
		
		/** Use context dependent interpolation: 
		 * NONE above 2x, 
		 * CUBIC below 0.5x, 
		 * LINEAR otherwise. */
		ADAPTIVE
	};

	/**
	 * The Enum LoopType.
	 */
	public static enum LoopType {

		/** Play forwards without looping. */
		NO_LOOP_FORWARDS, 

		/** Play backwards without looping. */
		NO_LOOP_BACKWARDS, 

		/** Play forwards with loop. */
		LOOP_FORWARDS, 

		/** Play backwards with loop. */
		LOOP_BACKWARDS, 

		/** Loop alternately forwards and backwards. */
		LOOP_ALTERNATING

	};

	/** The Sample. */
	protected Sample buffer;            

	/** The sample rate, determined by the Sample. */
	protected float sampleRate;                 

	/** The position in milliseconds. */
	protected double position;                

	/** The position envelope. */
	protected UGen positionEnvelope;

	/** The rate envelope. */
	protected UGen rateEnvelope;               

	/** The millisecond position increment per sample. Calculated from the ratio of the {@link AudioContext}'s sample rate and the {@link Sample}'s sample rate. */
	protected double positionIncrement;           

	/** Flag for alternating loop mode to determine if playback is in forward or reverse phase. */
	protected boolean forwards;

	/** The interpolation type. */
	protected InterpolationType interpolationType;

	/** The loop start envelope. */
	protected UGen loopStartEnvelope;              

	/** The loop end envelope. */
	protected UGen loopEndEnvelope;               

	/** The loop type. */
	protected LoopType loopType;

	/** The loop cross fade in milliseconds. */
	protected float loopCrossFade;                 			//TODO loop crossfade behaviour

	/** Flag to determine whether playback starts at the beginning of the sample or at the beginning of the loop. */
	protected boolean startLoop;

	/** Flag to determine whether the SamplePlayer should kill itself when it gets to the end of the Sample. */
	protected boolean killOnEnd;

	/** The rate. Calculated and used internally from the rate envelope. */
	protected float rate;

	/** The loop start. Calculated and used internally from the loop start envelope. */
	protected float loopStart;

	/** The loop end. Calculated and used internally from the loop end envelope. */
	protected float loopEnd;
	
	/** Array for temp storage. */
	protected float[] frame;

	/**
	 * Instantiates a new SamplePlayer with given number of outputs.
	 * 
	 * @param context the AudioContext.
	 * @param outs the number of outputs.
	 */
	public SamplePlayer(AudioContext context, int outs) {
		super(context, outs);
		rateEnvelope = new Static(context, 1.0f);
		positionEnvelope = null;
		interpolationType = InterpolationType.ADAPTIVE;
		loopType = LoopType.NO_LOOP_FORWARDS;
		forwards = true;
		killOnEnd = true;
		loopStartEnvelope = new Static(context, 0.0f);
		loopEndEnvelope = new Static(context, 0.0f);
	}

	/**
	 * Instantiates a new SamplePlayer with given Sample. Number of outputs is determined by number of channels in Sample.
	 * 
	 * @param context the AudioContext.
	 * @param buffer the Sample.
	 */
	public SamplePlayer(AudioContext context, Sample buffer) {
		this(context, buffer.getNumChannels());
		setBuffer(buffer);
		loopEndEnvelope.setValue(buffer.getLength());
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
	 * Determines whether the playback position is within the loop points.
	 * 
	 * @return true if the playback position is within the loop points.
	 */
	public boolean inLoop() {
		return position < Math.max(loopStart, loopEnd) && position > Math.min(loopStart, loopEnd);
	}

	/**
	 * Sets the playback position to the loop start point.
	 */
	public void setToLoopStart() {
		position = Math.min(loopStart, loopEnd);
		forwards = (rate > 0);
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

	/**
	 * Gets the position envelope. 
	 * 
	 * @return the position envelope.
	 */
	public UGen getPositionEnvelope() {
		return positionEnvelope;
	}

	/**
	 * Sets the position envelope. Setting the position envelope means that the position is then controlled by this envelope. If the envelope is null the position continues to be modified by the SamplePlayer's internal playback or by calls to change the position.
	 * 
	 * @param positionEnvelope the new position envelope.
	 */
	public void setPositionEnvelope(UGen positionEnvelope) {
		this.positionEnvelope = positionEnvelope;
	}

	/**
	 * Gets the rate envelope.
	 * 
	 * @return the rate envelope.
	 */
	public UGen getRateEnvelope() {
		return rateEnvelope;
	}

	/**
	 * Sets the rate envelope.
	 * 
	 * @param rateEnvelope the new rate envelope.
	 */
	public void setRateEnvelope(UGen rateEnvelope) {
		this.rateEnvelope = rateEnvelope;
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
	 * Gets the loop cross fade.
	 * 
	 * @return the loop cross fade in milliseconds.
	 */
	public float getLoopCrossFade() {
		return loopCrossFade;
	}

	/**
	 * Sets the loop cross fade.
	 * 
	 * @param loopCrossFade the new loop cross fade in milliseconds.
	 */
	public void setLoopCrossFade(float loopCrossFade) {
		this.loopCrossFade = loopCrossFade;
	}

	/**
	 * Gets the loop end envelope.
	 * 
	 * @return the loop end envelope.
	 */
	public UGen getLoopEndEnvelope() {
		return loopEndEnvelope;
	}

	/**
	 * Sets the loop end envelope.
	 * 
	 * @param loopEndEnvelope the new loop end envelope.
	 */
	public void setLoopEndEnvelope(UGen loopEndEnvelope) {
		this.loopEndEnvelope = loopEndEnvelope;
	}

	/**
	 * Gets the loop start envelope.
	 * 
	 * @return the loop start envelope
	 */
	public UGen getLoopStartEnvelope() {
		return loopStartEnvelope;
	}

	/**
	 * Sets the loop start envelope.
	 * 
	 * @param loopStartEnvelope the new loop start envelope.
	 */
	public void setLoopStartEnvelope(UGen loopStartEnvelope) {
		this.loopStartEnvelope = loopStartEnvelope;
	}

	/**
	 * Sets both loop points to static values as fractions of the Sample length.
	 * 
	 * @param start the start value, as fraction of the Sample length.
	 * @param end the end value, as fraction of the Sample length.
	 */
	public void setLoopPointsFraction(float start, float end) {
		loopStartEnvelope = new Static(context, start * (float)buffer.getLength());
		loopEndEnvelope = new Static(context, end * (float)buffer.getLength());
	}

	/**
	 * Gets the loop type.
	 * 
	 * @return the loop type.
	 */
	public LoopType getLoopType() {
		return loopType;
	}

	/**
	 * Sets the loop type.
	 * 
	 * @param loopType the new loop type.
	 */
	public void setLoopType(LoopType loopType) {
		this.loopType = loopType;
		if(loopType != LoopType.LOOP_ALTERNATING) {
			if(loopType == LoopType.LOOP_FORWARDS || loopType == LoopType.NO_LOOP_FORWARDS) {
				forwards = true;
			} else {
				forwards = false;
			}
		}
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
			if(positionEnvelope != null) {
				positionEnvelope.update();
			} else {
				//major speed up possible here if these envelopes are all either null or paused 
				//(can we pause Envelope when it is not doing anything?).
				//if this holds true we can tell buffer to just grab the whole frame at the given rate
				//and then update the position all at once.
				rateEnvelope.update();
				loopStartEnvelope.update();
				loopEndEnvelope.update();
			}
			for (int i = 0; i < bufferSize; i++) {
				//calculate the samples		
				switch (interpolationType) {
				case ADAPTIVE: 
					if(rate > ADAPTIVE_INTERP_HIGH_THRESH) {
						buffer.getFrameNoInterp(position, frame);
					} else if(rate > ADAPTIVE_INTERP_LOW_THRESH) {
						buffer.getFrameLinear(position, frame);
					} else {
						buffer.getFrameCubic(position, frame);
					}
					break;
				case LINEAR:
					buffer.getFrameLinear(position, frame);
					break;
				case CUBIC:
					buffer.getFrameCubic(position, frame);
					break;
				case NONE:
					buffer.getFrameNoInterp(position, frame);
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
		if(positionEnvelope != null) {
			position = positionEnvelope.getValue(0, i);
		} else {
			rate = rateEnvelope.getValue(0, i);
			switch(loopType) {
			case NO_LOOP_FORWARDS:
				position += positionIncrement * rate;
				if(position > buffer.getLength() || position < 0) atEnd();
				break;
			case NO_LOOP_BACKWARDS:
				position -= positionIncrement * rate;
				if(position > buffer.getLength() || position < 0) atEnd();
				break;
			case LOOP_FORWARDS:
				loopStart = loopStartEnvelope.getValue(0, i);
				loopEnd = loopEndEnvelope.getValue(0, i);
				position += positionIncrement * rate;
				if(rate > 0 && position > Math.max(loopStart, loopEnd)) {
					position = Math.min(loopStart, loopEnd);
				} else if(rate < 0 && position < Math.min(loopStart, loopEnd)) {
					position = Math.max(loopStart, loopEnd);
				}
				break;
			case LOOP_BACKWARDS:
				loopStart = loopStartEnvelope.getValue(0, i);
				loopEnd = loopEndEnvelope.getValue(0, i);
				position -= positionIncrement * rate;
				if(rate > 0 && position < Math.min(loopStart, loopEnd)) {
					position = Math.max(loopStart, loopEnd);
				} else if(rate < 0 && position > Math.max(loopStart, loopEnd)) {
					position = Math.min(loopStart, loopEnd);
				}
				break;
			case LOOP_ALTERNATING:
				loopStart = loopStartEnvelope.getValue(0, i);
				loopEnd = loopEndEnvelope.getValue(0, i);
				position += forwards ? positionIncrement * rate : -positionIncrement * rate;
				if(forwards ^ (rate < 0)) { 
					if(position > Math.max(loopStart, loopEnd)) {
						forwards = (rate < 0);
						position = 2 * Math.max(loopStart, loopEnd) - position;
					}
				} else if(position < Math.min(loopStart, loopEnd)) {
					forwards = (rate > 0);
					position = 2 * Math.min(loopStart, loopEnd) - position;
				}
				break;
			}
		}
	}


}
