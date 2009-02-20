//Much code taken from MEAP

package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.core.AudioContext;

// TODO: Auto-generated Javadoc
/**
 * The Class MFCC.
 */
public class MFCC extends MelSpectrum {

	/** The mfccs. */
	private float[] mfccs;
	
	/**
	 * Instantiates a new mFCC.
	 * 
	 * @param ac
	 *            the ac
	 * @param numCoeffs
	 *            the num coeffs
	 */
	public MFCC(float bufferSize, int numCoeffs) {
		super(bufferSize, numCoeffs);
		mfccs = new float[numCoeffs];
		featureDescriptions = new String[numCoeffs];
		for (int i = 0; i < numCoeffs; i++) {
			if(i < 9) featureDescriptions[i] = "mfcc0" + (i + 1);
			else featureDescriptions[i] = "mfcc" + (i + 1);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.MelSpectrum#calculateFeatures(float[])
	 */
	public void process(float[] powerSpectrum, int length) {
        // precompute DCT matrix
        int nmel = features.length;  
        double m = Math.sqrt(2.0/nmel);
        double[][] DCTcoeffs = new double[nmel][features.length];
        for(int i = 0; i < nmel; i++) {
            for(int j = 0; j < features.length; j++) {
                DCTcoeffs[i][j] = m*Math.cos(Math.PI*(j+1)*(i+.5)/(double)nmel);
            }
        }
        super.process(powerSpectrum, length);
        // convert to cepstrum:
        for(int x = 0; x < features.length; x++) {
            // convert from dB to plain old log magnitude
            features[x] = features[x]/10;  
            // take DCT
            for(int y = 0; y < features.length; y++) {
                mfccs[y] = (float)(DCTcoeffs[x][y]*features[x]);
            }
        }
        //printFeatures();
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.MelSpectrum#getFeatures()
	 */
	public float[] getFeatures() {
		return mfccs;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.analysis.MelSpectrum#getNumFeatures()
	 */
	public int getNumFeatures() {
		return mfccs.length;
	}

	/**
	 * Prints the features.
	 */
	private void printFeatures() {
		for (int i = 0; i < mfccs.length; i++) {
			System.out.print(mfccs[i] + " ");
		}
		System.out.println();
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
