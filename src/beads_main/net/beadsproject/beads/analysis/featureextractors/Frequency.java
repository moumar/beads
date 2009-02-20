//Much code borrowed from MEAP

package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.AudioContext;


// TODO: Auto-generated Javadoc
/**
 * The Class Frequency.
 */
public class Frequency extends FeatureExtractor {

	/** The Constant FIRSTBAND. */
	static final int FIRSTBAND = 3;
	
	/** The bin2hz. */
	private float bin2hz;
	
	/**
	 * Instantiates a new frequency.
	 * 
	 * @param context
	 *            the context
	 */
	public Frequency(AudioContext context) {
		bin2hz = context.getSampleRate() / (2 * (context.getBufferSize() - 1));
		features = new float[1];
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.PowerSpectrumListener#calculateFeatures(float[])
	 */
	public synchronized void process(float[] powerSpectrum, int length) {
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
		//double c = yz - k * Math.pow(x0, 2);
		features[0] = (float)(bin2hz * (maxbin + x0));
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
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#getFeatureDescriptions()
	 */
	public String[] getFeatureDescriptions() {
		return new String[]{"frequency"};
	}


}
