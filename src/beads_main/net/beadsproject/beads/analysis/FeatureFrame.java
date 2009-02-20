package net.beadsproject.beads.analysis;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;



public class FeatureFrame implements Serializable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private double startTimeMS;
	private double endTimeMS;
	Hashtable<String, float[]> features;
	
	public FeatureFrame(double startTimeMS, double endTimeMS) {
		super();
		this.startTimeMS = startTimeMS;
		this.endTimeMS = endTimeMS;
		features = new Hashtable<String, float[]>();
	}
	
	public double getStartTimeMS() {
		return startTimeMS;
	}
	
	public void setStartTimeMS(double startTimeMS) {
		this.startTimeMS = startTimeMS;
	}

	public double getEndTimeMS() {
		return endTimeMS;
	}
	
	public void setEndTimeMS(double endTimeMS) {
		this.endTimeMS = endTimeMS;
	}
	
	public void add(String s, float[] f) {
		features.put(s, f);
	}
	
	public float[] get(String s) {
		return features.get(s);
	}
	
	public Enumeration<String> keys() {
		return features.keys();
	}

	public boolean containsTime(double timeMS) {
		return timeMS > startTimeMS && timeMS < endTimeMS;
	}
	
	public String toString() {
		String result = "";
		result += "Start Time: " + startTimeMS + " (ms)";
		result += "\nEnd Time  : " + endTimeMS + " (ms)";
		for(String s : features.keySet()) {
			result += "\n" + s + ": ";
			float[] data = features.get(s);
			for(int i = 0; i < data.length; i++) {
				result += data[i] + " ";
			}
		}
		return result;
	}
	
}