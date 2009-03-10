package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;

import net.beadsproject.beads.analysis.FeatureExtractor;

public class SlowPowerSpectrum extends FeatureExtractor<float[],float[]>{
	
	protected ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	public SlowPowerSpectrum() {
		super();
		listeners = new ArrayList<FeatureExtractor<?, float[]>>();
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.FFT#calculateBuffer()
	 */
	@Override
	public void process(float[] fftdata) {
		// fftdata is in the format real1,imag1,real2,imag2,...
		// we ignore the redundant half of the fftdata		
		features = new float[fftdata.length/4];		
		numFeatures = fftdata.length/4;
		
		for(int i=0;i<fftdata.length/2;i+=2)
		{
			features[i/2] = (float)Math.hypot((double)fftdata[i],(double)fftdata[i+1]);			
		}
		
		//update the listeners
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(features);
		}
	}	
	
	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}
}
