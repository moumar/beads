package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;

// TODO: Auto-generated Javadoc
/**
 * The Class PowerSpectrum.
 */
public class PowerSpectrum extends FFT {

	/** The power spectrum. */
	private float[] powerSpectrum;
	
	public PowerSpectrum() {
		super();
		setNumberOfFeatures(10);
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.FFT#calculateBuffer()
	 */
	public void process(float[] data, int length) {
		//use superclass, FFT, to get fftReal and fftImag
		fft(data, length, true);
		fftReal = getReal(data, length);
		fftImag = getImag(data, length);
		//calculate the power spectrum
		calculatePower();
		//update the listeners
		for(FeatureExtractor fe : listeners) {
			fe.process(powerSpectrum, length / 2);
		}
	}
	
	/**
	 * Calculate power.
	 */
	private void calculatePower() {
		powerSpectrum = new float[fftReal.length / 2];
		for(int i = 0; i < fftReal.length / 2; i++) {
			powerSpectrum[i] = (float)Math.sqrt(fftReal[i] * fftReal[i] + fftImag[i] * fftImag[i]);
		}
	}
	
	/**
	 * Gets the binned power.
	 * 
	 * @param bins
	 *            the bins
	 * 
	 * @return the binned power
	 */
	public synchronized float[] getBinnedPower(int bins) {
		float[] binnedPower = new float[bins];
		for(int i = 0; i < binnedPower.length; i++) {
			binnedPower[i] = 0.0f;
		}
		if(powerSpectrum != null) {
			float scale = (float)powerSpectrum.length 
							/ (float)binnedPower.length;
			for(int i = 0; i < powerSpectrum.length; i++) {
//				binnedPower[(int)((float)binnedPower.length * (1f - 1f / (1f - 1f /Math.log((float)i / (float)powerSpectrum.length))))] += powerSpectrum[i];
				binnedPower[(int)((float)binnedPower.length * Math.pow((float)i / (float)powerSpectrum.length, 0.75f))] += powerSpectrum[i];
			}
			for(int i = 0; i < binnedPower.length; i++) {
				binnedPower[i] /= scale;
				if(Float.isNaN(binnedPower[i])) binnedPower[i] = 1f;
				//System.out.print(binnedPower[i] + " ");
			}
			//System.out.println();
		}
		return binnedPower;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#getFeatures()
	 */
	public float[] getFeatures() {
		return getBinnedPower(numFeatures); 
	}




}
