/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 * CREDIT: This class uses portions of code taken from MEAP. See readme/CREDITS.txt.
 */
package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;

import net.beadsproject.beads.analysis.FeatureExtractor;

/**
 * The Class MelSpectrum.
 */
public class MelSpectrum extends FeatureExtractor<float[], float[]>  {

	/** The ac. */
	private final float sampleRate;
	private int bufferSize;
	// for each mel bin...
	/** The mel center. */
	protected double[] melCenter; // actual targe mel value at center of this
	// bin
	/** The mel width. */
	protected double[] melWidth; // mel width divisor for this bin (constant,
	// except broadens in low bins)
	// for each fft bin
	/** The mel of lin. */
	protected double[] melOfLin;
	

	protected ArrayList<FeatureExtractor<?, float[]>> listeners;


	/**
	 * Instantiates a new mel spectrum.
	 * 
	 * @param ac
	 *            the ac
	 * @param numCoeffs
	 *            the num coeffs
	 */
	public MelSpectrum(float sampleRate, int numCoeffs) {
		this.sampleRate = sampleRate;
		setNumberOfFeatures(numCoeffs);
		bufferSize = -1;
		listeners = new ArrayList<FeatureExtractor<?,float[]>>();
	}
	
	private void setup() {
		int twiceBufferSize = bufferSize * 2;
		features = new float[numFeatures];
		// Calculate the locations of the bin centers on the mel scale and
		// as indices into the input vector
		melCenter = new double[numFeatures + 2];
		melWidth = new double[numFeatures + 2];
		double melMin = lin2mel(0);
		double melMax = lin2mel((8000.0 < sampleRate / 2) ? 8000.0 : sampleRate / 2); // dpwe 2006-12-11 - hard maximum
		double hzPerBin = sampleRate / 2 / twiceBufferSize;
		for (int i = 0; i < numFeatures + 2; i++) {
			melCenter[i] = melMin + i * (melMax - melMin) / (numFeatures + 1);
		}
		for (int i = 0; i < numFeatures + 1; i++) {
			melWidth[i] = melCenter[i + 1] - melCenter[i];
			double linbinwidth = (mel2lin(melCenter[i + 1]) - mel2lin(melCenter[i]))
					/ hzPerBin;
			if (linbinwidth < 1) {
				melWidth[i] = lin2mel(mel2lin(melCenter[i]) + hzPerBin)
						- melCenter[i];
			}
		}
		// precalculate mel translations of fft bin frequencies
		melOfLin = new double[twiceBufferSize];
		for (int i = 0; i < twiceBufferSize; i++) {
			melOfLin[i] = lin2mel(i * sampleRate / (2 * twiceBufferSize));
		}
		featureDescriptions = new String[numFeatures];
		for (int i = 0; i < numFeatures; i++) {
			if(i < 9) featureDescriptions[i] = "mel0" + (i + 1);
			else featureDescriptions[i] = "mel" + (i + 1);
		}
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.PowerSpectrumListener#calculateFeatures(float[])
	 */
	public void process(float[] powerSpectrum) {
		if(powerSpectrum.length != bufferSize) {
			bufferSize = powerSpectrum.length;
			setup();
		}
		float[] linSpec = new float[powerSpectrum.length];
		// convert log magnitude to linear magnitude for binning
		for (int band = 0; band < linSpec.length; band++)
			linSpec[band] = (float) Math.pow(10, powerSpectrum[band] / 10);
		// convert to mel scale
		for (int bin = 0; bin < features.length; bin++) {
			// initialize
			features[bin] = 0;
			for (int i = 0; i < linSpec.length; ++i) {
				//System.out.println(i + " " + linSpec.length);
				double weight = 1.0 - (Math.abs(melOfLin[i] - melCenter[bin]) / melWidth[bin]);
				if (weight > 0) {
					features[bin] += weight * linSpec[i];
				}
			}
			// Take log
			features[bin] = Math.max(0f, (float)(10f * Math.log(features[bin]) / Math.log(10)));
		}
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(features);
		}
	}
	
	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}


	/**
	 * Prints the features.
	 */
	private void printFeatures() {
		for (int i = 0; i < features.length; i++) {
			System.out.print(features[i] + " ");
		}
		System.out.println();
	}
	
	public void setNumberOfFeatures(int numFeatures) {
		super.setNumberOfFeatures(numFeatures);
		setup();
	}

	/**
	 * Lin2mel.
	 * 
	 * @param fq
	 *            the fq
	 * 
	 * @return the double
	 */
	public double lin2mel(double fq) {
		return 1127.0 * Math.log(1.0 + fq / 700.0);
	}

	/**
	 * Mel2lin.
	 * 
	 * @param mel
	 *            the mel
	 * 
	 * @return the double
	 */
	public double mel2lin(double mel) {
		return 700.0 * (Math.exp(mel / 1127.0) - 1.0);
	}


}
