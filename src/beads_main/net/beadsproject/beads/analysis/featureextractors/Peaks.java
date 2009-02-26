/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
//Much code borrowed from MEAP

package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.AudioContext;

// TODO: Auto-generated Javadoc
/**
 * The Class Peaks.
 */
public class Peaks extends FeatureExtractor<float[], float[]>  {

	/** The Constant FIRSTBAND. */
	static final int FIRSTBAND = 3;
	
	/** The bin2hz. */
	private float bin2hz;
	
	/**
	 * Instantiates a new peaks.
	 * 
	 * @param context
	 *            the context
	 */
	public Peaks(AudioContext context) {
		bin2hz = context.getSampleRate() / (2 * (context.getBufferSize() - 1));
		setNumberOfFeatures(10);
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.PowerSpectrumListener#calculateFeatures(float[])
	 */
	public synchronized void process(float[] powerSpectrum) {
		// collect average linear spectrum
		double[] linSpec = new double[powerSpectrum.length];
		for (int band = 0; band < linSpec.length; band++) {
			linSpec[band] = Math.pow(10, powerSpectrum[band] / 10);
		}
		// now pick best peaks from linspec
		for(int i = 0; i < features.length; i++) {
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
			//double c = yz - k * Math.pow(x0, 2);
			features[i] = (float)(bin2hz * (maxbin + x0));
			if(Float.isNaN(features[i]) || features[i] < 0f) features[i] = 0f;	//hack
			linSpec[maxbin] = -1f;
		}
		//printFeatures();
	}

	/**
	 * Prints the features.
	 */
	public void printFeatures() {
		for(int i = 0; i < features.length; i++) {
			System.out.print(features[i]);
		}
		System.out.println();
	}


	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#setNumFeatures(int)
	 */
	public void setNumberOfFeatures(int numFeatures) {
		features = new float[numFeatures];
		featureDescriptions = new String[numFeatures];
		for(int i = 0; i < numFeatures; i++) {
			featureDescriptions[i] = "peak" + i;
		}
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
