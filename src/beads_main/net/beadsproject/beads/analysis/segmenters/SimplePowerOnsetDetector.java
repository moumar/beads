
package net.beadsproject.beads.analysis.segmenters;

import net.beadsproject.beads.analysis.Segmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clicker;
import net.beadsproject.beads.ugens.RTInput;

// TODO: Auto-generated Javadoc
/**
 * The Class OnsetDetector.
 */
public class SimplePowerOnsetDetector extends Segmenter {

	//I haven't implemented the recorder in this yet, so this Segmenter can't pass audio to FeatureExtractors yet
	
	
    /** The BLOCK. */
    static private int BLOCK = 10;
    
    /** The beat strength. */
    private int beatStrength;
    
    /** The thresholds. */
    private float[] thresholds;
    
    /** The count. */
    private int count;
    
    /** The hop. */
    private int hop;
    
    /** The cutout. */
    private int cutout;
    
    /** The cutout count. */
    private int cutoutCount;
    
    /** The click. */
    private boolean click;
    
    /** The local count. */
    private int localCount;
    
    /**
	 * Instantiates a new onset detector.
	 * 
	 * @param context
	 *            the context
	 * @param listener
	 *            the listener
	 */
    public SimplePowerOnsetDetector(AudioContext context, Bead listener) {
    	this(context);
    	addListener(listener);
    }
    
    /**
	 * Instantiates a new onset detector.
	 * 
	 * @param context
	 *            the context
	 */
    public SimplePowerOnsetDetector(AudioContext context) {
        super(context);
        setThresholds(new float[] {0.1f, 0.2f, 0.4f});
        count = 0;
        cutout = (int) context.msToSamples(20f);
        cutoutCount = -1;
        setHop(5f);
        setClick(false);
    }
    
    
    
    
	/**
	 * Gets the cutout.
	 * 
	 * @return the cutout
	 */
	public double getCutout() {
		return context.samplesToMs(cutout);
	}

	
	/**
	 * Sets the cutout.
	 * 
	 * @param cutout
	 *            the new cutout
	 */
	public void setCutout(float cutout) {
		this.cutout = (int)context.msToSamples(cutout);
	}

    
	/**
	 * Checks if is clicking.
	 * 
	 * @return true, if is clicking
	 */
	public boolean isClicking() {
		return click;
	}

	
	/**
	 * Sets the click.
	 * 
	 * @param click
	 *            the new click
	 */
	public void setClick(boolean click) {
		this.click = click;
	}

	/**
	 * Gets the hop.
	 * 
	 * @return the hop
	 */
	public double getHop() {
        return context.samplesToMs(hop);
    }
    
    /**
	 * Sets the hop.
	 * 
	 * @param msHop
	 *            the new hop
	 */
    public void setHop(float msHop) {
        hop = (int)context.msToSamples(msHop);
        hop = hop - (hop  % BLOCK);
    }
    
	/**
	 * Gets the thresholds.
	 * 
	 * @return the thresholds
	 */
	public float[] getThresholds() {
		return thresholds;
	}

	/**
	 * Sets the thresholds.
	 * 
	 * @param thresholds
	 *            the new thresholds
	 */
	public void setThresholds(float[] thresholds) {
		this.thresholds = thresholds;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
    public void calculateBuffer() {
    	if(count > cutoutCount) {
	        localCount = 0;
	        while(localCount < bufferSize - BLOCK) {
	            //grab average of next 4 samples
	            float average = 0.0f;
	            for(int i = 0; i < BLOCK; i++) {
	                average += bufIn[0][localCount++];
	            }
	            //System.out.println(localCount + " " + average);
	            average /= (float)BLOCK;
	            //System.out.println(average);
	            beatStrength = 0;
	            for(int i = 0; i < thresholds.length; i++) {
		            if(average > thresholds[i]) {
		            	beatStrength = i + 1;
		            }
	            }
	            if(beatStrength > 0) {
	                onset();
	                cutoutCount = count + localCount + cutout;
	                break;
	            } 
	            localCount += hop;
	        }   
    	}
        count += bufferSize;
    }
    	
    /**
	 * Gets the beat strength.
	 * 
	 * @return the beat strength
	 */
    public int getBeatStrength() {
    	return beatStrength;
    }
	
    /** The btcnt. */
    int btcnt = 0;
    
    /**
	 * Onset.
	 */
    private void onset() {
    	if(click) context.out.addInput(new Clicker(context, 0.5f));
    	segment(null, 0);
    }

    /**
	 * Time in ms.
	 * 
	 * @return the float
	 */
    private double timeInMS() {
    	//time in samples is count + localCount
    	return context.samplesToMs(count + localCount);
    }
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		System.out.println("OnsetDetectorTest");
		AudioContext ac = new AudioContext(512, 1500);
		ac.start();
		//SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		RTInput input = new RTInput(ac, ac.getAudioFormat());
		SimplePowerOnsetDetector od = new SimplePowerOnsetDetector(ac);
		od.setClick(true);
		od.addInput(input);
		ac.out.addDependent(od);
		//ac.getRoot().addInput(sp);
	}
    
}
