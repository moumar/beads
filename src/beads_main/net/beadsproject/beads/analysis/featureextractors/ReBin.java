package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;


public class ReBin extends FeatureExtractor<float[], float[]> {

	public ReBin(int numFeatures) {
		this.numFeatures = numFeatures;
	}
	
	@Override
	public void process(float[] original) {
		features = new float[numFeatures];
		int factor = original.length / features.length;
		if(original != null) {
			float scale = (float)original.length / (float)features.length;
			for(int i = 0; i < original.length; i++) {
				features[i / factor] += original[i];
			}
			for(int i = 0; i < features.length; i++) {
				features[i] /= scale;
				if(Float.isNaN(features[i])) features[i] = 0f;
			}
		}
	}

}
