/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

/**
 * FeatureExtractor is an abstract base class for classes that perform some kind of analysis on audio data. The resulting feature data is assumed to take the form of an array of floats. Implementing classes use the method {@link #process(float[], int)} to process data. This method gets passed an array of audio data and an integer specifying the length of that array to analyse. The incoming data may not be raw sample data, but may be the output of an FFT or some other transform instead, so it is important to check which feature data the feature extractor works with before plugging it in.
 *
 * @author ollie
 */
public abstract class FeatureExtractor {
	
	/** The number of features. */
	protected int numFeatures;
	
	/** The current array of feature data. */
	protected float[] features;
	
	/** The name of the FeatureExtractor. */
	protected String name;
	
	/** An array of Strings providing descriptions of the feature data. */
	protected String[] featureDescriptions;
	
	/**
	 * Instantiates a new FeatureExtractor. This constructor names the FeatureExtractor with the name of the implementing class.
	 */
	public FeatureExtractor() {
		name = getClass().getSimpleName();
	}
	
	/**
	 * Process some data. A length argument is provided, which is useful in certain cases such as when a large buffer of audio is being passed, but only a small part need be analysed, or the case of an FFT where only half of the data is needed.
	 * 
	 * @param data the data.
	 * @param length the length of the data to use. 
	 */
	public abstract void process(float[] data, int length);
	
	/**
	 * Gets the current features.
	 * 
	 * @return the features.
	 */
	public float[] getFeatures() {
		return features;
	}
	
	/**
	 * Gets the number of features.
	 * 
	 * @return the number of features.
	 */
	public int getNumberOfFeatures() {
		return numFeatures;
	}
	
	/**
	 * Sets the number of features.
	 * 
	 * @param numFeatures the new number of features.
	 */
	public void setNumberOfFeatures(int numFeatures) {
		this.numFeatures = numFeatures;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the feature descriptions. Implementing classes should make sure that this array has meaningful content.
	 * 
	 * @return the feature descriptions.
	 */
	public String[] getFeatureDescriptions() {
		return featureDescriptions;
	}

}

