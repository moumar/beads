package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import java.util.Arrays;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.MeanFilter;

/** 
 * Detects Peaks/Onsets in input.
 * It updates listeners when peaks in energy are detected 
 * Assumes input is a 1 element array (i.e., a single value)
 * 
 * @author ben
 */

public class PeakDetector extends FeatureExtractor<Float, Float>{
	private float threshold = 0;
	private float base_threshold = 0;
	private int M = 4;
	private float lastMValues[];
	private Buffer filter;	
	private BeadArray listeners;
	
	public PeakDetector(){
		super();		
		listeners = new BeadArray();
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
	public void process(Float input) {
		float value = input;		
		boolean exceededThreshold = false;
		if (value > threshold)
			exceededThreshold = true;		
		
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
		
		// simple onset detection mechanism 
		if (exceededThreshold)
		{
			// we can be rising or falling
			// we want the point in between these two
			
			// if rising then wait
			// if falling after risen then a local maxima has just occurred
			// NOTE: we are 1 step/frame/segment behind
			if (lastMValues[M-2] > lastMValues[M-3] && value < lastMValues[M-2])
			{				
				// notify the listeners 
				features = value;
				listeners.message(this);	//any use for a time stamp here?
			}			
		}		
	}
	
	public void addListener(Bead listener) {
		listeners.add(listener);
	}

}
