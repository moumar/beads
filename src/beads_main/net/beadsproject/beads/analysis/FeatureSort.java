package net.beadsproject.beads.analysis;

import java.util.Collection;
import java.util.TreeMap;


public class FeatureSort {

	public static Collection<Double> distanceFromTarget(FeatureTrack track, String featureName, float[] target) {
		TreeMap<Float, Double> sorting = new TreeMap<Float, Double>();
		for(FeatureFrame ff : track) {
			float[] featureData = (float[])ff.get(featureName);
			float distance = distance(target, featureData);
			sorting.put(distance, ff.getStartTimeMS());
		}
		Collection<Double> orderedList = sorting.values();
		return orderedList;
	}
	
	public static float distance(float[] a, float[] b) {
		float result = 0f;
		for(int i = 0; i < a.length; i++) {
			result += Math.pow(a[i] - b[i], 2f);
		}
		result = (float)Math.sqrt(result);
		return result;
	}
}
