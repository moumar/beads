package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import net.beadsproject.beads.analysis.FeatureExtractor;


public class SpectralDifference extends FeatureExtractor<float[], float[]> {

	private float[] previousSpectrum;
	private int minBin;
	private int maxBin;
	protected ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	public SpectralDifference() {
		features = new float[1];
		listeners = new ArrayList<FeatureExtractor<?,float[]>>();
		setMinBin(10);		//TODO really these should be set as a fraction of the total bins
		setMaxBin(100);
	}
	
	public int getMinBin() {
		return minBin;
	}
	
	public void setMinBin(int minBin) {
		this.minBin = Math.max(0, minBin);
	}
	
	public int getMaxBin() {
		return maxBin;
	}
	
	public void setMaxBin(int maxBin) {
		this.maxBin = Math.max(0, maxBin);
	}
	
	/**
	 * Adds a FeatureExtractor as a listener.
	 * 
	 * @param the FeatureExtractor.
	 */
	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}

	@Override
	public void process(float[] spectrum) {
		int bins = maxBin - minBin;
		float spectralDifference = 0f;
		if(bins > 0) {
			if(previousSpectrum == null || previousSpectrum.length != bins) {
				previousSpectrum = new float[bins];
			}
			for(int i = 0; i < bins; i++) {
				float thisDiff = spectrum[minBin + i] - previousSpectrum[i];
				spectralDifference += thisDiff * thisDiff;
				previousSpectrum[i] = spectrum[minBin + i];
			}
			spectralDifference = (float)Math.sqrt(spectralDifference / bins);
			features[0] = spectralDifference;
		}
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(features);
		}
	}
	
}
