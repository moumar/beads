/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
//Much code taken from MEAP

package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import java.util.Arrays;

import net.beadsproject.beads.analysis.FeatureExtractor;

// TODO: Auto-generated Javadoc
/**
 * The Class MFCC.
 */
public class MFCC extends FeatureExtractor<float[], float[]> {


	private ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	private double[][] DCTcoeffs; 
	
	private int inputLength;
	
	/**
	 * Instantiates a new mFCC.
	 * 
	 * @param ac
	 *            the ac
	 * @param numCoeffs
	 *            the num coeffs
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
	
	public void setNumberOfFeatures(int num) {
		super.setNumberOfFeatures(num);
		inputLength = -1; //flag to make sure DCTcoeffs are setup
	}
	
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
	 * Prints the features.
	 */
	private void printFeatures() {
		for (int i = 0; i < features.length; i++) {
			System.out.print(features[i] + " ");
		}
		System.out.println();
	}

	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}

}
