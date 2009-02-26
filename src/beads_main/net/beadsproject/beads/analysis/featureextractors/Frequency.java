/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 * CREDIT: This class uses portions of code taken from MEAP. See readme/CREDITS.txt.
 */
package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.AudioContext;

/**
 * Frequency processes spectral data forwarded to it by a {@link PowerSpectrum} to determine the best estimate for the frequency of the current signal.
 */
public class Frequency extends FeatureExtractor<float[], float[]> {

	/** The Constant FIRSTBAND. */
	static final int FIRSTBAND = 3;
	
	/** The ratio bin2hz. */
	private float bin2hz;
	
	private int bufferSize;
	
	private float sampleRate;
	
	/**
	 * Instantiates a new Frequency.
	 * 
	 * @param context
	 *            the AudioContext.
	 */
	public Frequency(float sampleRate) {
		bufferSize = -1;
		this.sampleRate = sampleRate;
		features = new float[1];
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.PowerSpectrumListener#calculateFeatures(float[])
	 */
	public synchronized void process(float[] powerSpectrum) {
		if(bufferSize != powerSpectrum.length) {
			bufferSize = powerSpectrum.length;
			bin2hz = sampleRate / (2 * (bufferSize - 1));
		}
		features = new float[1];
		// collect average linear spectrum
		double[] linSpec = new double[powerSpectrum.length];
		for (int band = 0; band < linSpec.length; band++) {
			linSpec[band] = Math.pow(10, powerSpectrum[band] / 10);
		}
		// now pick best peak from linspec
		double pmax = -1;
		int maxbin = 0;
		for (int band = FIRSTBAND; band < powerSpectrum.length; band++) {
			// double pwr = pitchWt[band]*linSpec[band];
			double pwr = linSpec[band];
			if (pwr > pmax) {
				pmax = pwr;
				maxbin = band;
			}
		}
		// cubic interpolation
		double yz = linSpec[maxbin];
		double ym = maxbin <= 0? linSpec[maxbin] : linSpec[maxbin - 1];
		double yp = maxbin < linSpec.length - 1 ? linSpec[maxbin + 1] : linSpec[maxbin];
		double k = (yp + ym) / 2 - yz;
		double x0 = (ym - yp) / (4 * k);
		features[0] = (float)(bin2hz * (maxbin + x0));
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#getFeatureDescriptions()
	 */
	public String[] getFeatureDescriptions() {
		return new String[]{"frequency"};
	}


}
