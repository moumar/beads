/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;

/**
 * PowerSpectrum calculates the power spectrum from a segmented audio signal. PowerSpectrum forwards the full power spectrum data to its listeners.
 */
public class PowerSpectrum extends FFT {

	/** The power spectrum data. */
	private float[] powerSpectrum;
	
	/**
	 * Instantiates a new PowerSpectrum.
	 */
	public PowerSpectrum() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.FFT#calculateBuffer()
	 */
	public void process(float[] data) {
		//use superclass, FFT, to get fftReal and fftImag
		float[] dataCopy = new float[data.length];
		for(int i = 0; i < data.length; i++) {
			dataCopy[i] = data[i];
		}
		fft(dataCopy, dataCopy.length, true);
		fftReal = calculateReal(dataCopy, dataCopy.length);
		fftImag = calculateImaginary(dataCopy, dataCopy.length);
		//calculate the power spectrum
		calculatePower();
		//update the listeners
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(powerSpectrum);
		}
	}
	
	/**
	 * Calculate the power from the fft data.
	 */
	private void calculatePower() {
		setNumberOfFeatures(fftReal.length / 2);
		powerSpectrum = new float[fftReal.length / 2];
		for(int i = 0; i < fftReal.length / 2; i++) {
			powerSpectrum[i] = (float)Math.sqrt(fftReal[i] * fftReal[i] + fftImag[i] * fftImag[i]);
		}
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#getFeatures()
	 */
	public float[] getFeatures() {
		return powerSpectrum;
	}




}
