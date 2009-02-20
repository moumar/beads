/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;

// TODO: Auto-generated Javadoc
/**
 * The Class SamplePlayer.
 */
public class SamplePlayer extends UGen {

    /**
	 * The Enum InterpolationType.
	 */
    public static enum InterpolationType {
        
        /** The LINEAR. */
        LINEAR, 
 /** The CUBIC. */
 CUBIC
    };
       
    /**
	 * The Enum LoopType.
	 */
    public static enum LoopType {
        
        /** The N o_ loo p_ forwards. */
        NO_LOOP_FORWARDS, 
 /** The N o_ loo p_ backwards. */
 NO_LOOP_BACKWARDS, 
 /** The LOO p_ forwards. */
 LOOP_FORWARDS, 
 /** The LOO p_ backwards. */
 LOOP_BACKWARDS, 
 /** The LOO p_ alternating. */
 LOOP_ALTERNATING
    };
    
    /** The buffer. */
    protected Sample buffer = null;            
    
    /** The sample rate. */
    protected float sampleRate;                 
    
    /** The position. */
    protected double position;                    
    
    /** The last changed position. */
    protected double lastChangedPosition;
    
    /** The position envelope. */
    protected UGen positionEnvelope;
    
    /** The rate envelope. */
    protected UGen rateEnvelope;               
    
    /** The position increment. */
    protected double positionIncrement;           
    
    /** The forwards. */
    protected boolean forwards;                    //are we going forwards? (if not we're going backwards)
    
    /** The interpolation type. */
    protected InterpolationType interpolationType;
    
    /** The loop start envelope. */
    protected UGen loopStartEnvelope;              
    
    /** The loop end envelope. */
    protected UGen loopEndEnvelope;               
    
    /** The loop type. */
    protected LoopType loopType;
    
    /** The loop cross fade. */
    protected float loopCrossFade;                 			//TODO loop crossfade behaviour
    
    /** The start loop. */
    protected boolean startLoop;							//TODO behaviour such that you're outside the loop points you immediately pop inside them

    protected boolean killOnLoopEnd;
    
    /**
	 * Instantiates a new sample player.
	 * 
	 * @param context
	 *            the context
	 */
    protected SamplePlayer(AudioContext context, int outs) {
        super(context, outs);
        rateEnvelope = new Static(context, 1.0f);
        positionEnvelope = new Static(context, 0.0f);
        interpolationType = InterpolationType.LINEAR;
        loopType = LoopType.NO_LOOP_FORWARDS;
        forwards = true;
        killOnLoopEnd = true;
    }

    /**
	 * Instantiates a new sample player.
	 * 
	 * @param context
	 *            the context
	 * @param buffer
	 *            the buffer
	 */
    public SamplePlayer(AudioContext context, Sample buffer) {
        this(context, buffer.nChannels);
        setBuffer(buffer);
        loopStartEnvelope = new Static(context, 0.0f);
        loopEndEnvelope = new Static(context, buffer.length);
    }

    /**
	 * Sets the buffer.
	 * 
	 * @param buffer
	 *            the new buffer
	 */
    protected void setBuffer(Sample buffer) {
        this.buffer = buffer;
        sampleRate = buffer.audioFormat.getSampleRate();
        updatePosInc();
    }
    
    /**
	 * Gets the buffer.
	 * 
	 * @return the buffer
	 */
    public Sample getBuffer() {
    	return buffer;
    }

    /**
	 * Sets the to end.
	 */
    public void setToEnd() {
        position = buffer.nFrames;
    }
    
    /**
	 * Start.
	 * 
	 * @param msPosition
	 *            the ms position
	 */
    public void start(float msPosition) {
        position = msPosition;
        start();
    }
    
    /**
	 * Reset.
	 */
    public void reset() {
        position = 0f;
    }
    
    /**
	 * Gets the position.
	 * 
	 * @return the position
	 */
    public double getPosition() {
    	return context.samplesToMs((float)position);
    }
    
    public void setPosition(double position) {
    	this.position = context.msToSamples(position);
    }
    
	/**
	 * Gets the position envelope.
	 * 
	 * @return the position envelope
	 */
	public UGen getPositionEnvelope() {
		return positionEnvelope;
	}

	/**
	 * Sets the position envelope.
	 * 
	 * @param positionEnvelope
	 *            the new position envelope
	 */
	public void setPositionEnvelope(UGen positionEnvelope) {
		this.positionEnvelope = positionEnvelope;
	}

	/**
	 * Gets the rate envelope.
	 * 
	 * @return the rate envelope
	 */
	public UGen getRateEnvelope() {
        return rateEnvelope;
    }

    /**
	 * Sets the rate envelope.
	 * 
	 * @param rateEnvelope
	 *            the new rate envelope
	 */
    public void setRateEnvelope(UGen rateEnvelope) {
        this.rateEnvelope = rateEnvelope;
    }

    /**
	 * Update pos inc.
	 */
    private void updatePosInc() {
        positionIncrement = context.samplesToMs(sampleRate / context.getSampleRate());
    }

    /**
	 * Gets the interpolation type.
	 * 
	 * @return the interpolation type
	 */
    public InterpolationType getInterpolationType() {
        return interpolationType;
    }

    /**
	 * Sets the interpolation type.
	 * 
	 * @param interpolationType
	 *            the new interpolation type
	 */
    public void setInterpolationType(InterpolationType interpolationType) {
        this.interpolationType = interpolationType;
    }

    /**
	 * Gets the loop cross fade.
	 * 
	 * @return the loop cross fade
	 */
    public float getLoopCrossFade() {
        return (float)buffer.samplesToMs(loopCrossFade);
    }

    /**
	 * Sets the loop cross fade.
	 * 
	 * @param loopCrossFade
	 *            the new loop cross fade
	 */
    public void setLoopCrossFade(float loopCrossFade) {
        this.loopCrossFade = (float)buffer.msToSamples(loopCrossFade);
    }

    /**
	 * Gets the loop end envelope.
	 * 
	 * @return the loop end envelope
	 */
    public UGen getLoopEndEnvelope() {
        //return buffer.samplesToMs(loopEnd);
    	return loopEndEnvelope;
    }

    /**
	 * Sets the loop end envelope.
	 * 
	 * @param loopEndEnvelope
	 *            the new loop end envelope
	 */
    public void setLoopEndEnvelope(UGen loopEndEnvelope) {
        //this.loopEnd = buffer.msToSamples(loopEnd);
    	this.loopEndEnvelope = loopEndEnvelope;
    }

    /**
	 * Gets the loop start envelope.
	 * 
	 * @return the loop start envelope
	 */
    public UGen getLoopStartEnvelope() {
        //return buffer.samplesToMs(loopStart);
    	return loopStartEnvelope;
    }

    /**
	 * Sets the loop start envelope.
	 * 
	 * @param loopStartEnvelope
	 *            the new loop start envelope
	 */
    public void setLoopStartEnvelope(UGen loopStartEnvelope) {
        //this.loopStart = buffer.msToSamples(loopStart);
    	this.loopStartEnvelope = loopStartEnvelope;
    }
    
    /**
	 * Sets the loop points fraction.
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 */
    public void setLoopPointsFraction(float start, float end) {
        loopStartEnvelope = new Static(context, start * (float)buffer.length);
        loopEndEnvelope = new Static(context, end * (float)buffer.length);
    }

    /**
	 * Gets the loop type.
	 * 
	 * @return the loop type
	 */
    public LoopType getLoopType() {
        return loopType;
    }

    /**
	 * Sets the loop type.
	 * 
	 * @param loopType
	 *            the new loop type
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
	 * @return the sample rate
	 */
    public float getSampleRate() {
        return sampleRate;
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
//    	System.out.println("samplePos " + position + " rate " + positionIncrement * rateEnvelope.getValue(0, 0) + " buf " + bufferSize);
    	rateEnvelope.update();
    	positionEnvelope.update();
    	loopStartEnvelope.update();
    	loopEndEnvelope.update();
        for (int i = 0; i < bufferSize; i++) {
            //calculate the samples
//        	double posInSamples = context.msToSamples((float)position);
        	double posInSamples = buffer.msToSamples((float)position);
            int currentSample = (int) posInSamples;
            float fractionOffset = (float)(posInSamples - currentSample);
            float[] frame = null;
            switch (interpolationType) {
                case LINEAR:
                    frame = buffer.getFrameLinear(currentSample, fractionOffset);
                    break;
                case CUBIC:
                    frame = buffer.getFrameCubic(currentSample, fractionOffset);
                    break;
            }
            for (int j = 0; j < outs; j++) {
                bufOut[j][i] = frame[j];
            }
            //update the position, loop state, direction
            calculateNextPosition(i);
            if(isPaused()) {
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
    
    /** The rate. */
    protected float rate;
    
    /** The loop start. */
    protected float loopStart;
    
    /** The loop end. */
    protected float loopEnd;
    
    public void setKillOnLoopEnd(boolean killOnLoopEnd) {
    	this.killOnLoopEnd = killOnLoopEnd;
    }

    public boolean getKillOnLoopEnd() {
    	return(killOnLoopEnd);
    }
    
    private void atLoopEnd () {
    	if (killOnLoopEnd) {
    		kill();
    	}
    	else {
    		pause(true);
    	}
    }
    
    public void reTrigger() {
    	reset();
    	this.pause(false);
    }
    
    /**
	 * Calculate next position.
	 * 
	 * @param i
	 *            the i
	 */
    public void calculateNextPosition(int i) {
    	rate = rateEnvelope.getValue(0, i);
    	loopStart = loopStartEnvelope.getValue(0, i);
    	loopEnd = loopEndEnvelope.getValue(0, i);
    	if(lastChangedPosition != positionEnvelope.getValue(0, i)) {
    		position = positionEnvelope.getValue(0, i);
    		lastChangedPosition = (float)position;
    	}
        switch(loopType) {
            case NO_LOOP_FORWARDS:
                position += positionIncrement * rate;
                if(position > buffer.length || position < 0) atLoopEnd();
                break;
            case NO_LOOP_BACKWARDS:
                position -= positionIncrement * rate;
                if(position > buffer.length || position < 0) atLoopEnd();
                break;
            case LOOP_FORWARDS:
                position += positionIncrement * rate;
                if(rate > 0 && position > Math.max(loopStart, loopEnd)) {
                    position = Math.min(loopStart, loopEnd);
                } else if(rate < 0 && position < Math.min(loopStart, loopEnd)) {
                    position = Math.max(loopStart, loopEnd);
                }
                break;
            case LOOP_BACKWARDS:
                position -= positionIncrement * rate;
                if(rate > 0 && position < Math.min(loopStart, loopEnd)) {
                    position = Math.max(loopStart, loopEnd);
                } else if(rate < 0 && position > Math.max(loopStart, loopEnd)) {
                    position = Math.min(loopStart, loopEnd);
                }
                break;
            case LOOP_ALTERNATING:
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
    
    public boolean inLoop() {
    	return position < Math.max(loopStart, loopEnd) && position > Math.min(loopStart, loopEnd);
    }
    
    public void snapToLoopStart() {
    	position = Math.min(loopStart, loopEnd);
    	forwards = (rate > 0);
    }
    
}
