package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.FeatureExtractor;


// TODO: Auto-generated Javadoc
/**
 * The Class Power.
 */
public class Power extends FeatureExtractor {

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
	public void process(float[] audioData, int length) {
		features[0] = 0.0f;
		for(int i = 0; i < length; i++) {
			features[0] += audioData[i] * audioData[i];
		}
		features[0] = (float)Math.sqrt(features[0] / (float)length);
	}

	
	
}






