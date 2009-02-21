/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
//Much code taken from MEAP

package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;

// TODO: Auto-generated Javadoc
/**
 * The Class SpectralCentroid.
 */
public class SpectralCentroid extends FeatureExtractor {

	/** The ac. */
	private float sampleRate;
	
	/**
	 * Instantiates a new spectral centroid.
	 * 
	 * @param ac
	 *            the ac
	 */
	public SpectralCentroid(float sampleRate) {
		features = new float[1];
		this.sampleRate = sampleRate;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.PowerSpectrumListener#calculateFeatures(float[])
	 */
	public void process(float[] powerSpectrum, int length) {
		double num = 0;
		double den = 0;
		num = 0;
		den = 0;
		for (int band = 0; band < length; band++) {
			double freqCenter = band * (sampleRate / 2)
					/ (length - 1);
			// convert back to linear power
			double p = Math.pow(10, powerSpectrum[band] / 10);
			num += freqCenter * p;
			den += p;
		}
		features[0] = (float) (num / den);
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#setNumFeatures(int)
	 */
	public void setNumFeatures(int numFeatures) {
		//Not allowed
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {

	}

}
