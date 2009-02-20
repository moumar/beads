package net.beadsproject.beads.analysis;


public abstract class FeatureExtractor {
	
	protected int numFeatures;
	protected float[] features;
	protected String name;
	protected String[] featureDescriptions;
	
	public FeatureExtractor() {
		name = getClass().getSimpleName();
	}
	
	public abstract void process(float[] data, int length);
	
	public float[] getFeatures() {
		return features;
	}
	
	public int getNumberOfFeatures() {
		return numFeatures;
	}
	
	public void setNumberOfFeatures(int numFeatures) {
		this.numFeatures = numFeatures;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getFeatureDescriptions() {
		return featureDescriptions;
	}

}

