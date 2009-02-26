package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;

import net.beadsproject.beads.analysis.FeatureExtractor;


public class ReBin extends FeatureExtractor<float[], float[]> {

	private ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	public ReBin(int numFeatures) {
		this.numFeatures = numFeatures;
		listeners = new ArrayList<FeatureExtractor<?,float[]>>();
	}
	
	@Override
	public void process(float[] original) {
		features = new float[numFeatures];
		if(original != null) {
			float scale = (float)original.length / (float)features.length;
			for(int i = 0; i < original.length; i++) {
				features[(int)(i / scale)] += original[i];
			}
			for(int i = 0; i < features.length; i++) {
				features[i] /= scale;
				if(Float.isNaN(features[i])) features[i] = 0f;
			}
		}
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(features);
		}
	}
	
	public void addListener(FeatureExtractor<?, float[]> listener) {
		listeners.add(listener);
	}

}
