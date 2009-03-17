package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import java.util.Arrays;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.MeanFilter;

/** 
 * Detects peaks in a continuous stream of one element inputs. 
 * Attach to an OnsetDetectionFunction (like SpectralDifference) to get Onsets.
 * Use addMessageListener to receive a message when an onset is detected.
 * 
 * The algorithm follows the one described in:
 *    Dixon, S (2006) "Onset Detection Revisited" 
 *    Proc. of the 9th Int. Conference on Digital Audio Effects (DAFx-06), Montreal, Canada, September 18-20, 2006
 * 
 * @author ben
 */

public class OnsetDetector extends FeatureExtractor<float[], float[]>{
	
	/** thresholdListeners receive the filtered threshold value 
	 * TODO: DISABLE AFTER DEBUG, BP170309
	 */
	protected ArrayList<FeatureExtractor<?, float[]>> thresholdListeners;
	/** listeners receive a message when an onset is detected */
    private BeadArray listeners;
        
    private float valueAtOnset = 0;    
    private float threshold = 0;    
	private float base_threshold = 0;
	
	private float lastValues[];
	private Buffer filter;
	
	/** size of window to search for local maxima (lag will then be floor(W/2) frames) */
	private final int W = 3;
	private final int WM = 3; // multiplier
	private final int M = W + WM*W + 1;
	private float alpha = 0.9f;
	
	public OnsetDetector(){
		super();
		thresholdListeners = new ArrayList<FeatureExtractor<?,float[]>>();
		listeners = new BeadArray();
	
		features = new float[1];				
		
		lastValues = new float[M];
		Arrays.fill(lastValues,0.f);
		filter = new MeanFilter().generateBuffer(M);
	}
	
	public void setThreshold(float thresh)
	{
		base_threshold = thresh;
	}
	
	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}
	
	/** 
	 * @return the value at the last onset
	 */
	public float getLastOnsetValue()
	{
		return valueAtOnset;	
	}
	
	/**
	 * @return The lag in frames between onsets occurring and actually being detected  
	 */
	public int getLagInFrames()
	{
		return W;
	}
	
	/**
	 * Get the correct BufferSize for the OnsetDetector 
	 * Any 
	 * @return
	 */
	public int getBufferSize()
	{
		return M;
	}
	
	/**
	 * Sets the window for the local averaging. 
	 * @param b Buffer must be of size == getBufferSize(), and integrates to 1.
	 */
	public void setFilter(Buffer b)
	{
		assert (b.buf.length == M);
		filter = b;		
	}
	
	/** 
	 * process: assumes input is a 1 element array
	 */
	@Override
	public void process(float[] input) {
		assert input.length==1;		
		float value = input[0];
		
		// cache the values		
		for(int i=1;i<M;i++)
		{
			lastValues[i-1] = lastValues[i];
		}		
		lastValues[M-1] = value;
		
		// simple onset detection mechanism
		// from simon dixon paper...
		// Trigger a peak at (M-1-W) if 
		// 1. lastMValues[M-1-W] > threshold (DONE)		
		// 2. lastMValues[M-1-W] > lastMValues[M-1-..]..lastMValues[M-1]
		// 3. lastMValues[M-1-W] > average of lastMValues[M-1-.. .. M-1]
		float lastValue = lastValues[M-1-W];
		/* notify the threshold Listeners */
		features[0] = threshold;
		for(FeatureExtractor<?,float[]> fe: thresholdListeners)
			fe.process(features);
				
		if (lastValue > threshold)
		{		
			boolean passedTest2 = true;
			for(int i=M-1-2*W;i<=M-1;i++)
			{
				if (i==M-1-W) continue;
				if (lastValue < lastValues[i])
				{
					passedTest2 = false;
				}
			}
			
			if (passedTest2)
			{
				// apply the FIR filter
				float average = 0;
				for(int i=1;i<M;i++)
				{				
					average += lastValues[i]*filter.buf[i];
				}
				average += value*filter.buf[M-1];
								
				if (lastValue > average+base_threshold)				
				{
					// All tests have passed, therefore we have detected a peak->thus an onset
					valueAtOnset = lastValue;			
					listeners.message(this);					
				}
			}				
		}		
		
		// update the threshold function
		threshold = Math.max(lastValue, alpha*threshold + (1-alpha)*lastValue);		
	}

	public void addMessageListener(Bead b) {
		listeners.add(b);
	}
	
    public void removeMessageListener(Bead b) {
        listeners.remove(b);
    }
	
	public void addThresholdListener(FeatureExtractor<?, float[]> fe){
		thresholdListeners.add(fe);
	}
	
	
}
