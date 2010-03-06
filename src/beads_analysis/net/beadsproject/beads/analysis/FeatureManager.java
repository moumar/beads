package net.beadsproject.beads.analysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;

public class FeatureManager {
	
	private final static Map<Sample, FeatureSet> featureSets = new Hashtable<Sample, FeatureSet>();

	private static boolean verbose = true;
	
	/**
	 * Sets the FeatureSet for the given Sample.
	 * @param s the Sample.
	 * @param fs the FeatureSet.
	 */
	public static void setFeaturesForSample(Sample s, FeatureSet fs) {
		featureSets.put(s, fs);
	}

	/**
	 * Gets the FeatureSet for a given Sample. The method first checks to see if the FeatureSet
	 * is already stored in memory. If not it looks for a file with the same file name as the 
	 * Sample (including the file type), but with the suffix ".features". Once loaded, the FeatureSet
	 * is stored in memory.
	 * 
	 * @param sample the Sample to search for features of.
	 * @return the FeatureSet.
	 */
	public static FeatureSet featuresForSample(Sample sample) {
		if(featureSets.containsKey(sample)) {
			return featureSets.get(sample);
		} 
		FeatureSet set = FeatureSet.forSample(sample);
		if(set != null) {
			featureSets.put(sample, set);
			if(verbose) System.out.println("Loaded features for " + sample.getFileName());
		}
		return set;
	}

	public static void featuresForGroup(String groupName) {
		ArrayList<Sample> theSamples = SampleManager.getGroup(groupName);
		for(Sample s : theSamples) {
			featuresForSample(s);
		}
	}
	
	/**
	 * Removes the features assosciated with the given {@link Sample}.
	 * 
	 * @param s the Sample.
	 */
	public static void removeSample(Sample s) {
		if(featureSets.containsKey(s)) {
			featureSets.remove(s);
		}
	}

	/**
	 * Determines if FeatureManager is being verbose.
	 * 
	 * @return true if verbose.
	 */
	public static boolean isVerbose() {
		return verbose;
	}

	/**
	 * Tells FeatureManager to produce verbose output.
	 * 
	 * @param verbose true for verbose output.
	 */
	public static void setVerbose(boolean verbose) {
		FeatureManager.verbose = verbose;
	}
}
