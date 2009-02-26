/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;


// TODO: Auto-generated Javadoc
/**
 * The Class Power.
 */
public class Power extends FeatureExtractor<float[], float[]>  {

	public Power() {
		features = new float[1];
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#getFeatureDescriptions()
	 */
	public String[] getFeatureDescriptions() {
		return new String[] {"Power"};
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#getNumFeatures()
	 */
	public int getNumberOfFeatures() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.FrameFeatureExtractor#setNumFeatures(int)
	 */
	public void setNumberOfFeatures(int numFeatures) {
		//not allowed
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void process(float[] audioData) {
		features[0] = 0.0f;
		for(int i = 0; i < audioData.length; i++) {
			features[0] += audioData[i] * audioData[i];
		}
		features[0] = (float)Math.sqrt(features[0] / (float)audioData.length);
	}

	
	
}






