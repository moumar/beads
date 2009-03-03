package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;

import net.beadsproject.beads.analysis.FeatureExtractor;

/** PeakDetector: Updates listeneres when peaks in energy are detected 
 * Assumes input is a 1 element array with values greater than 0
 * 
 * @author ben
 *
 */

public class PeakDetector extends FeatureExtractor<float[], float[]>{
	protected ArrayList<FeatureExtractor<?, float[]>> listeners;
	float threshold;
	float base_threshold;
	float sum;
	int M;
	float lastMValues[];	
	
	public PeakDetector(){
		super();
		listeners = new ArrayList<FeatureExtractor<?,float[]>>();	
		threshold = 0;
		base_threshold = 0;
		sum = 0;		
		M = 128;
		lastMValues = new float[M];
		for(int i=0;i<M;i++)
			lastMValues[i] = 0;
		features = new float[1];		
	}
	
	/** 
	 * process: assumes input is a 1 element array
	 */
	@Override
	public void process(float[] input) {
		assert input.length==1;
		// if input is above the threshold value then notify the listeners
		float value = input[0];
		boolean fire = false;
		if (value > threshold)
			fire = true;		
		
		// update the threshold value based on a moving average
		// threshold += (sum_i=0toM d(n-i))/(M+1)
		sum -= lastMValues[0];
		// shift values
		for(int i=1;i<M;i++)
			lastMValues[i-1] = lastMValues[i];
		lastMValues[M-1] = value;
		sum += value;
		threshold = base_threshold + sum/(M+1);
		
		features[0] = fire?1:0;
		for(FeatureExtractor<?, float[]> fe : listeners)
			fe.process(features);
		
	}

	/**
	 * Adds a FeatureExtractor as a listener.
	 * 
	 * @param the FeatureExtractor.
	 */
	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}
}
