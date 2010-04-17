/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;

/**
 * SimpleSamplePlayer plays back a {@link Sample}.
 * It has limited functionality but is faster than SamplePlayer.
 * 
 * The playback point in the {@link Sample} can also be directly controlled from {@link UGen} to perform scrubbing.
 * @author Ben
 */
public class SimpleSamplePlayer extends UGen {

	/**
	 * The Enum LoopType.
	 */
	public static enum LoopType {

		/** Play forwards without looping. */
		NO_LOOP_FORWARDS, 

		/** Play forwards with loop. */
		LOOP_FORWARDS
	};

	/** The Sample. */
	protected Sample buffer;            

	/** The sample rate, determined by the Sample. */
	protected float sampleRate;                 

	/** The position in milliseconds. */
	protected double position;                    

	/** The last changed position, used to determine if the position envelope is in use. */
	protected double lastChangedPosition;

	/** The position envelope. */
	protected UGen positionEnvelope;
 
	/** The millisecond position increment per sample. Calculated from the ratio of the {@link AudioContext}'s sample rate and the {@link Sample}'s sample rate. */
	protected double positionIncrement;           

	/** Flag for alternating loop mode to determine if playback is in forward or reverse phase. */
	protected boolean forwards;

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

	/**
	 * Instantiates a new SamplePlayer with given number of outputs.
	 * 
	 * @param context the AudioContext.
	 * @param outs the number of outputs.
	 */
	public SimpleSamplePlayer(AudioContext context, int outs) {
		super(context, outs);
		positionEnvelope = new Static(context, 0.0f);
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
	public SimpleSamplePlayer(AudioContext context, Sample buffer) {
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
	 * Sets the playback position.
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
	 * Sets the position envelope. Setting the position envelope means that the position is then controlled by this envelope. If the envelope is null, or unchanging, the position continues to be modified by the SamplePlayer's internal playback or by calls to change the position.
	 * 
	 * @param positionEnvelope the new position envelope.
	 */
	public void setPositionEnvelope(UGen positionEnvelope) {
		this.positionEnvelope = positionEnvelope;
	}

	/**
	 * Updates the position increment. Called whenever the {@link Sample}'s sample rate or the {@link AudioContext}'s sample rate is modified.
	 */
	private void updatePositionIncrement() {
		positionIncrement = context.samplesToMs(sampleRate / context.getSampleRate());
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
		this.forwards = true; // always true in simple sample player
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
			positionEnvelope.update();
			loopStartEnvelope.update();
			loopEndEnvelope.update();
			
			position += positionEnvelope.getValue();
			
			long posInSamples = (long)(buffer.msToSamples((float)position));		
			long samplesToEnd = buffer.getNumFrames()-posInSamples;
			if (samplesToEnd < bufferSize)
			{
				// then on the boundary to split up...
				// atm just quit...
				atEnd();
			}
			else
			{
				buffer.getFrames((int) posInSamples, bufOut);
				position += buffer.samplesToMs(bufferSize);
			}
			
			
			/*
			switch(loopType) {
			case NO_LOOP_FORWARDS:
				position += positionIncrement * rate;
				if(position > buffer.getLength() || position < 0) atEnd();
				break;
			case LOOP_FORWARDS:
				position += positionIncrement * rate;
				if(rate > 0 && position > Math.max(loopStart, loopEnd)) {
					position = Math.min(loopStart, loopEnd);
				} else if(rate < 0 && position < Math.min(loopStart, loopEnd)) {
					position = Math.max(loopStart, loopEnd);
				}
			
			*/
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
}
