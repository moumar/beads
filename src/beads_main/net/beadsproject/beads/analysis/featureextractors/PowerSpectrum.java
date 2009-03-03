/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import net.beadsproject.beads.analysis.FeatureExtractor;

/**
 * PowerSpectrum calculates the power spectrum from the output of {@link FFT}. PowerSpectrum forwards the full power spectrum data to its listeners.
 */
public class PowerSpectrum extends FeatureExtractor<float[], float[][]> {

	protected ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	/**
	 * Instantiates a new PowerSpectrum.
	 */
	public PowerSpectrum() {
		listeners = new ArrayList<FeatureExtractor<?,float[]>>();
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.FFT#calculateBuffer()
	 */
	public void process(float[][] data) {
		if(features == null || features.length != data[0].length / 2) {
			features = new float[data[0].length / 2];
		}
		for(int i = 0; i < features.length; i++) {
			features[i] = (float)Math.sqrt(data[0][i] * data[0][i] + data[1][i] * data[1][i]);
		}
		//update the listeners
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(features);
		}
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
