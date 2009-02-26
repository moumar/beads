package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;


public class BinnedPowerSpectrum extends FeatureExtractor<float[], float[]> {

	@Override
	public void process(float[] powerSpectrum) {
		features = new float[numFeatures];
		for(int i = 0; i < features.length; i++) {
			features[i] = 0.0f;
		}
		if(powerSpectrum != null) {
			float scale = (float)powerSpectrum.length 
							/ (float)features.length;
			for(int i = 0; i < powerSpectrum.length; i++) {
//				binnedPower[(int)((float)binnedPower.length * (1f - 1f / (1f - 1f /Math.log((float)i / (float)powerSpectrum.length))))] += powerSpectrum[i];
				features[(int)((float)features.length * Math.pow((float)i / (float)powerSpectrum.length, 0.75f))] += powerSpectrum[i];
			}
			for(int i = 0; i < features.length; i++) {
				features[i] /= scale;
				if(Float.isNaN(features[i])) features[i] = 1f;
			}
		}
	}



}
