package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;

import net.beadsproject.beads.analysis.FeatureExtractor;
public class SlowFFTWrapper extends FeatureExtractor<float[], float[]>{
	protected ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	public SlowFFTWrapper() {
		super();
		listeners = new ArrayList<FeatureExtractor<?, float[]>>();		
	}
	
	Complex complexfeatures[];
	
	public void process(float[] data) {		
		Complex dat[] = new Complex[data.length];
		for(int i=0;i<data.length;i++)
		{
			dat[i] = new Complex(data[i],0);
		}
				
		complexfeatures = SlowFFT.fft(dat);
		
		features = new float[2*complexfeatures.length];
		for(int i=0;i<complexfeatures.length;i++)
		{
			features[2*i] = (float)complexfeatures[i].re();
			features[2*i+1] = (float)complexfeatures[i].im();			
		}
		
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(features);
		}
	}
	
	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}
}
