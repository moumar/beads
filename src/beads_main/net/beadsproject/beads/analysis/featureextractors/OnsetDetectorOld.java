package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import java.util.Arrays;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.MeanFilter;

/** 
 * Detects Onsets in input.
 * It updates listeners when onsets in energy are detected 
 * Assumes input is a 1 element array
 * 
 * @author ben
 */

public class OnsetDetectorOld extends FeatureExtractor<float[], float[]>{
	
	/** thresholdListeners receive the filtered threshold value 
	 * TODO: DISABLE AFTER DEBUG, BP170309
	 */
	protected ArrayList<FeatureExtractor<?, float[]>> thresholdListeners;
	/** listeners receive a message when an onset is detected */
    private BeadArray listeners;
        
    private float threshold = 0;
    private float valueAtOnset = 0; 
    private boolean isCurrentlyAboveThreshold = false;
    
	private float base_threshold = 0;	
	private int M = 5;
	private float lastMValues[];
	private Buffer filter;
	
	/** size of window to search for local maxima (lag will then be floor(W/2) frames) */
	private final int W = 3;	
	
	public OnsetDetectorOld(){
		super();
		thresholdListeners = new ArrayList<FeatureExtractor<?,float[]>>();
		listeners = new BeadArray();
	
		features = new float[1];				
		
		lastMValues = new float[M];
		Arrays.fill(lastMValues,0.f);
		filter = new MeanFilter().generateBuffer(M);
	}
	
	/**
	 * Tell the Peak Detector to use a moving average filter of a certain size. 
	 * 
	 * @param m Size of the filter. Larger = Smoother. M must be greater than 3. 
	 */
	public void useMeanFilterOfSize(int m)
	{
		M = m;
		lastMValues = new float[M];
		Arrays.fill(lastMValues,0.f);
		filter = new MeanFilter().generateBuffer(M);
	}
	
	public void setThreshold(float thresh)
	{
		base_threshold = thresh;		
	}
	
	/** 
	 * @return the value at the last onset
	 */
	public float getLastOnsetValue()
	{
		return valueAtOnset;	
	}
	
	/**
	 * Sets the window for the FIR filter. 
	 * @param b
	 */
	public void setFilter(Buffer b)
	{
		filter = b;
		M = b.buf.length;
		lastMValues = new float[M];
		Arrays.fill(lastMValues,0.f);		
	}
	
	/** 
	 * process: assumes input is a 1 element array
	 */
	@Override
	public void process(float[] input) {
		assert input.length==1;
		
		float value = input[0];		
		float lastThreshold = threshold;		
		
		// reapply the FIR filter
		// 1. shift the cached values and
		// 2. convolve the window with the cached input
		float sum = 0;
		for(int i=1;i<M;i++)
		{
			lastMValues[i-1] = lastMValues[i];
			sum += lastMValues[i]*filter.buf[i];
		}
		
		lastMValues[M-1] = value;
		sum += value*filter.buf[M-1];
		// 3. update the threshold		
		threshold = base_threshold + sum;
		
		/* notify the threshold Listeners */
		features[0] = threshold;
		for(FeatureExtractor<?,float[]> fe: thresholdListeners)
			fe.process(features);
		
		// simple onset detection mechanism
		// from simon dixon paper...
		// Trigger a peak at (M-1-W/2) if 
		// 1. lastMValues[M-1-W/2] > threshold (DONE)		
		// 2. lastMValues[M-1-W/2] > lastMValues[M-1-W]..lastMValues[M-1]
		// 3. lastMValues[M-1-W/2] > average of lastMValues[M-1-W .. M-1]
		float lastValue = lastMValues[M-1-W/2];
		if (lastValue > lastThreshold)
		{		
			boolean passedTest2 = true;
			float average = 0;
			for(int i=M-1-W;i<=M-1;i++)
			{
				if (i==M-1-W/2) continue;
				if (lastValue < lastMValues[i])
				{
					passedTest2 = false;
					break;
				}
				else
					average += lastMValues[i];
			}
			
			if (passedTest2)
			{
				average /= W;
				if (lastValue > average+threshold)				
				{
					// All tests haev passed, therefore we have detected a peak->thus an onset
					valueAtOnset = lastValue;			
					listeners.message(this);					
				}
			}
			
			// we can be rising or falling
			// we want the point in between these two
			
			// if rising then wait
			// if falling after risen then a local maxima has just occurred
			// NOTE: we are 1 step/frame/segment behind
			/*
			if (lastMValues[M-2] > lastMValues[M-3] && value < lastMValues[M-2])
			{				
				// notify the listeners 
				features[0] = value;
				for(FeatureExtractor<?, float[]> fe : listeners)
					fe.process(features);
			}
			*/	
		}
		
		
		/*
		if (exceededThreshold && !isCurrentlyAboveThreshold)
		{
			// if exceed threshold then notify messageListeners
			isCurrentlyAboveThreshold = true;
			valueAtOnset = value;			
			listeners.message(this);
			
					
		}
	*//*
		else if (!exceededThreshold)
		{
			isCurrentlyAboveThreshold = false;
		}*/
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
