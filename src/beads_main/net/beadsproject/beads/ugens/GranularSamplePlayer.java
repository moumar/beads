package net.beadsproject.beads.ugens;

import java.util.ArrayList;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.HanningWindow;

// TODO: Auto-generated Javadoc
/**
 * The Class GranularSamplePlayer.
 */
public class GranularSamplePlayer extends SamplePlayer {

    /** The pitch envelope. */
    private UGen pitchEnvelope;
    
    /** The grain interval envelope. */
    private UGen grainIntervalEnvelope;
    
    /** The grain size envelope. */
    private UGen grainSizeEnvelope;
    
    /** The randomness envelope. */
    private UGen randomnessEnvelope;
    
    /** The time since last grain. */
    private float timeSinceLastGrain;
    
    /** The ms per sample. */
    private double msPerSample;
    
    /** The pitch. */
    protected float pitch;
    
    /** The grains. */
    private ArrayList<Grain> grains;
    
    /** The free grains. */
    private ArrayList<Grain> freeGrains;
    
    /** The dead grains. */
    private ArrayList<Grain> deadGrains;
    
    /** The window. */
    private Buffer window;
    
    /** The frame. */
    private float[] frame;
    
    private boolean loopInsideGrains;

    /**
	 * The Class Grain.
	 */
    private static class Grain {
        
        /** The free. */
        boolean free;
        
        /** The start time. */
        double startTime;
        
        /** The position. */
        double position;
        
        /** The age. */
        double age;
        
        double grainSize;
    }

    /**
	 * Instantiates a new granular sample player.
	 * 
	 * @param context
	 *            the context
	 */
    private GranularSamplePlayer(AudioContext context, int outs) {
        super(context, outs);
        grains = new ArrayList<Grain>();
        freeGrains = new ArrayList<Grain>();
        deadGrains = new ArrayList<Grain>();
        pitchEnvelope = new Envelope(context, 1f);
        setGrainIntervalEnvelope(new Static(context, 70.0f));
        setGrainSizeEnvelope(new Static(context, 100.0f));
        setRandomnessEnvelope(new Static(context, 0.0f));
        window = new HanningWindow().getDefault();
        msPerSample = context.samplesToMs(1f);
        loopInsideGrains = false;
    }

    /**
	 * Instantiates a new granular sample player.
	 * 
	 * @param context
	 *            the context
	 * @param buffer
	 *            the buffer
	 */
    public GranularSamplePlayer(AudioContext context, Sample buffer) {
        this(context, buffer.nChannels);
        setBuffer(buffer);
        loopStartEnvelope = new Static(context, 0.0f);
        loopEndEnvelope = new Static(context, buffer.length);
    }

    /**
	 * Gets the pitch envelope.
	 * 
	 * @return the pitch envelope
	 */
    public UGen getPitchEnvelope() {
        return pitchEnvelope;
    }

    /**
	 * Sets the pitch envelope.
	 * 
	 * @param pitchEnvelope
	 *            the new pitch envelope
	 */
    public void setPitchEnvelope(UGen pitchEnvelope) {
        this.pitchEnvelope = pitchEnvelope;
    }

    /**
	 * Gets the grain interval envelope.
	 * 
	 * @return the grain interval envelope
	 */
    public UGen getGrainIntervalEnvelope() {
        return grainIntervalEnvelope;
    }

    /**
	 * Sets the grain interval envelope.
	 * 
	 * @param grainIntervalEnvelope
	 *            the new grain interval envelope
	 */
    public void setGrainIntervalEnvelope(UGen grainIntervalEnvelope) {
        this.grainIntervalEnvelope = grainIntervalEnvelope;
    }

    /**
	 * Gets the grain size envelope.
	 * 
	 * @return the grain size envelope
	 */
    public UGen getGrainSizeEnvelope() {
        return grainSizeEnvelope;
    }

    /**
	 * Sets the grain size envelope.
	 * 
	 * @param grainSizeEnvelope
	 *            the new grain size envelope
	 */
    public void setGrainSizeEnvelope(UGen grainSizeEnvelope) {
        this.grainSizeEnvelope = grainSizeEnvelope;
    }

    /**
	 * Gets the randomness envelope.
	 * 
	 * @return the randomness envelope
	 */
    public UGen getRandomnessEnvelope() {
		return randomnessEnvelope;
	}

	/**
	 * Sets the randomness envelope.
	 * 
	 * @param randomnessEnvelope
	 *            the new randomness envelope
	 */
	public void setRandomnessEnvelope(UGen randomnessEnvelope) {
		this.randomnessEnvelope = randomnessEnvelope;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#start()
	 */
	@Override
    public void start() {
        super.start();
        timeSinceLastGrain = 0;
    }
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.ugens.SamplePlayer#stop()
	 */
	public void kill() {
		super.kill();
	}
    
    /**
	 * Reset grain.
	 * 
	 * @param g
	 *            the g
	 * @param time
	 *            the time
	 */
    private void resetGrain(Grain g, int time) {
        g.startTime = (float)position + (grainSizeEnvelope.getValue(0, time) * randomnessEnvelope.getValue(0, time) * (float)(Math.random() * 2.0f - 1.0f));
        g.position = g.startTime;
        g.age = 0f;
        g.grainSize = grainSizeEnvelope.getValue(0, time);
    }   
    
    private boolean firstGrain = true;
    
    private void firstGrain() {
    	if(firstGrain) {
	    	Grain g = new Grain();
	    	g.startTime = -position / 2f;
	    	g.position = position;
	    	g.age = grainSizeEnvelope.getValue() / 2f;
	    	grains.add(g);
	    	firstGrain = false;
	    	timeSinceLastGrain = grainIntervalEnvelope.getValue() / 2f;
    	}
    }
    

    /* (non-Javadoc)
     * @see com.olliebown.beads.ugens.SamplePlayer#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
        //special condition for first grain
    	//update the various envelopes
    	rateEnvelope.update();
    	positionEnvelope.update();
    	loopStartEnvelope.update();
    	loopEndEnvelope.update();
    	pitchEnvelope.update();
    	grainIntervalEnvelope.update();
    	grainSizeEnvelope.update();
    	randomnessEnvelope.update();
        firstGrain();
    	//now loop through the buffer
        for (int i = 0; i < bufferSize; i++) {
            //determine if we need a new grain
            if (timeSinceLastGrain > grainIntervalEnvelope.getValue(0, i)) {
                if(freeGrains.size() > 0) {
                    Grain g = freeGrains.get(0);
                    freeGrains.remove(0);
                    resetGrain(g, i);
                    grains.add(g);
                } else {
                    Grain g = new Grain();
                    resetGrain(g, i);
                    grains.add(g);
                }
                timeSinceLastGrain = 0f;
            }
            //for each channel, start by resetting current output frame
            for (int j = 0; j < outs; j++) {
                bufOut[j][i] = 0.0f;
            }
            //gather the output from each grain
            for(int gi = 0; gi < grains.size(); gi++) {
                Grain g = grains.get(gi);
                //calculate value of grain window
                float windowScale = window.getValueFraction((float)(g.age / g.grainSize));
                //get position in sample for this grain
//                double samplePosition = context.msToSamples(g.position);
                double samplePosition = buffer.msToSamples((float)g.position);		//TODO doubles/floats sort out the mess
                int currentSample = (int)samplePosition;
                float fractionOffset = (float)(samplePosition - currentSample);
                //get the frame for this grain
                switch (interpolationType) {
                    case LINEAR:
                        frame = buffer.getFrameLinear(currentSample, fractionOffset);
                        break;
                    case CUBIC:
                        frame = buffer.getFrameCubic(currentSample, fractionOffset);
                        break;
                }
                //add it to the current output frame
                for (int j = 0; j < outs; j++) {
                    bufOut[j][i] += windowScale * frame[j];
                }
            }
            //increment time and stuff
            calculateNextPosition(i);
            //for(Grain g : grains) {
            pitch = Math.abs(pitchEnvelope.getValue(0, i));
            for(int gi = 0; gi < grains.size(); gi++) {
                Grain g = grains.get(gi);
                calculateNextGrainPosition(g);
            }
            if (isPaused()) {
                //make sure to zero the remaining outs
                while(i < bufferSize) {
                    for (int j = 0; j < outs; j++) {
                        bufOut[j][i] = 0.0f;
                    }
                    i++;
                }
                break;
            }
            //increment timeSinceLastGrain
            timeSinceLastGrain += msPerSample;
            //finally, see if any grains are dead
            for(int gi = 0; gi < grains.size(); gi++) {
                Grain g = grains.get(gi);
                if(g.age > g.grainSize) {
                    freeGrains.add(g);
                    deadGrains.add(g);
                }
            }
            for(int gi = 0; gi < deadGrains.size(); gi++) {
                Grain g = deadGrains.get(gi);
                grains.remove(g);
            }
            deadGrains.clear();
        }
    }
    
    /**
	 * Calculate next grain position.
	 * 
	 * @param g
	 *            the g
	 */
    private void calculateNextGrainPosition(Grain g) {
    	int direction = rate > 0 ? 1 : -1;
    	g.age += msPerSample;
    	if(loopInsideGrains) {
	        switch(loopType) {
	            case NO_LOOP_FORWARDS:
	                g.position += direction * positionIncrement * pitch;
	                break;
	            case NO_LOOP_BACKWARDS:
	                g.position -= direction * positionIncrement * pitch;
	                break;
	            case LOOP_FORWARDS:
	                g.position += direction * positionIncrement * pitch;
	                if(rate > 0 && g.position > Math.max(loopStart, loopEnd)) {
	                    g.position = Math.min(loopStart, loopEnd);
	                } else if(rate < 0 && g.position < Math.min(loopStart, loopEnd)) {
	                    g.position = Math.max(loopStart, loopEnd);
	                }
	                break;
	            case LOOP_BACKWARDS:
	                g.position -= direction * positionIncrement * pitch;
	                if(rate > 0 && g.position < Math.min(loopStart, loopEnd)) {
	                    g.position = Math.max(loopStart, loopEnd);
	                } else if(rate < 0 && g.position > Math.max(loopStart, loopEnd)) {
	                    g.position = Math.min(loopStart, loopEnd);
	                }
	                break;
	            case LOOP_ALTERNATING:
	                g.position += direction * (forwards ? positionIncrement * pitch : -positionIncrement * pitch);
	                if(forwards ^ (rate < 0)) {
	                	if(g.position > Math.max(loopStart, loopEnd)) {
	                        g.position = 2 * Math.max(loopStart, loopEnd) - g.position;
	                	}
	                } else if(g.position < Math.min(loopStart, loopEnd)) {
	                    g.position = 2 * Math.min(loopStart, loopEnd) - g.position;
	                }
	                break;
	        }   
    	} else {
    		g.position += direction * positionIncrement * pitch;
    	}
    }
  
    public float getAverageNumberOfGrains() {
    	return grainSizeEnvelope.getValue() / grainIntervalEnvelope.getValue();
    }
    
}
