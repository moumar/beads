/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 * CREDIT: This class uses portions of code taken from MEAP. See readme/CREDITS.txt.
 */
package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import java.util.Arrays;

import net.beadsproject.beads.analysis.FeatureExtractor;

/**
 * MFCC generates mel-frequency cepstral coefficients, an important feature type in music and speech processing. MFCC receives feature data from a {@link MelSpectrum}, and must be set as a listener to a {@link MelSpectrum}, not a pure audio stream, in order to work properly.
 */
public class MFCC extends FeatureExtractor<float[], float[]> {

	/** Array of listeners. */
	private ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	/** Matrix used for discrete cosine transform. */
	private double[][] DCTcoeffs; 
	
	/** Size of the input data. */
	private int inputLength;
	
	/**
	 * Instantiates a new MFCC.
	 * 
	 * @param numCoeffs the number of coefficients to generate.
	 */
	public MFCC(int numCoeffs) {
		setNumberOfFeatures(numCoeffs);
		features = new float[numFeatures];
		featureDescriptions = new String[numFeatures];
		for (int i = 0; i < numCoeffs; i++) {
			if(i < 9) featureDescriptions[i] = "mfcc0" + (i + 1);
			else featureDescriptions[i] = "mfcc" + (i + 1);
		}
		listeners = new ArrayList<FeatureExtractor<?,float[]>>();
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.analysis.FeatureExtractor#setNumberOfFeatures(int)
	 */
	public void setNumberOfFeatures(int num) {
		super.setNumberOfFeatures(num);
		inputLength = -1; //flag to make sure DCTcoeffs are setup
	}
	
	/**
	 * Builds the matrix of discrete cosine transform coefficients.
	 */
	private void setupDCTcoeffs() {
        double m = Math.sqrt(2.0 / inputLength);
        DCTcoeffs = new double[inputLength][features.length];
        for(int i = 0; i < inputLength; i++) {
            for(int j = 0; j < features.length; j++) {
                DCTcoeffs[i][j] = m * Math.cos(Math.PI * (j + 1) * (i + 0.5) / (double)inputLength);
            }
        }
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.MelSpectrum#calculateFeatures(float[])
	 */
	public void process(float[] melSpectrum) {
		Arrays.fill(features, 0f);
        // precompute DCT matrix
		float[] melSpectrumCopy = new float[melSpectrum.length];
		for(int i = 0; i < melSpectrum.length; i++) {
			melSpectrumCopy[i] = melSpectrum[i];
		}
		if(melSpectrum.length != inputLength) {
			inputLength = melSpectrum.length;
			setupDCTcoeffs();
		}
        // convert to cepstrum:
        for(int x = 0; x < melSpectrumCopy.length; x++) {
            // convert from dB to plain old log magnitude
        	melSpectrumCopy[x] = melSpectrumCopy[x]/10;  
            // take DCT
            for(int y = 0; y < features.length; y++) {
                features[y] += (float)(DCTcoeffs[x][y]*melSpectrumCopy[x]);
            }
        }
        for(FeatureExtractor<?, float[]> fe : listeners) {
        	fe.process(features);
        }
	}
	

	/**
	 * Prints the feature data.
	 */
	private void printFeatures() {
		for (int i = 0; i < features.length; i++) {
			System.out.print(features[i] + " ");
		}
		System.out.println();
	}

	/**
	 * Adds a listener to this MFCC.
	 * 
	 * @param fe the listener.
	 */
	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}

}
