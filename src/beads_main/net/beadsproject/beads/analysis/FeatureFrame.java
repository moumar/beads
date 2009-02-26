/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A FeatureFrame stores a set of features for a single chunk of audio data.
 *
 * @author ollie
 */
public class FeatureFrame implements Serializable {
		
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The start time in milliseconds. */
	private double startTimeMS;
	
	/** The end time in milliseconds. */
	private double endTimeMS;
	
	/** The features. */
	private Hashtable<String, Object> features;
	
	/**
	 * Instantiates a new FeatureFrame.
	 * 
	 * @param startTimeMS the start time in milliseconds.
	 * @param endTimeMS the end time in milliseconds.
	 */
	public FeatureFrame(double startTimeMS, double endTimeMS) {
		super();
		this.startTimeMS = startTimeMS;
		this.endTimeMS = endTimeMS;
		features = new Hashtable<String, Object>();
	}
	
	/**
	 * Gets the start time in milliseconds.
	 * 
	 * @return the start time in milliseconds.
	 */
	public double getStartTimeMS() {
		return startTimeMS;
	}
	
	/**
	 * Sets the start time in milliseconds.
	 * 
	 * @param startTimeMS the new start time in milliseconds.
	 */
	public void setStartTimeMS(double startTimeMS) {
		this.startTimeMS = startTimeMS;
	}

	/**
	 * Gets the end time in milliseconds.
	 * 
	 * @return the end time in milliseconds.
	 */
	public double getEndTimeMS() {
		return endTimeMS;
	}
	
	/**
	 * Sets the end time in milliseconds.
	 * 
	 * @param endTimeMS the new end time in milliseconds.
	 */
	public void setEndTimeMS(double endTimeMS) {
		this.endTimeMS = endTimeMS;
	}
	
	/**
	 * Adds a set of features with the given name.
	 * 
	 * @param s the name used to identify the feature set.
	 * @param f the features.
	 */
	public void add(String s, Object f) {
		features.put(s, f);
	}
	
	/**
	 * Gets the features for the given name.
	 * 
	 * @param s the name.
	 * 
	 * @return the features.
	 */
	public Object get(String s) {
		return features.get(s);
	}
	
	/**
	 * Returns an Enumeration over the set of names used to identify the features.
	 * 
	 * @return Enumeration over feature names.
	 */
	public Enumeration<String> keys() {
		return features.keys();
	}

	/**
	 * Checks whether the given time in milliseconds is within this frame.
	 * 
	 * @param timeMS the time in milliseconds.
	 * 
	 * @return true the frame contains this point in time.
	 */
	public boolean containsTime(double timeMS) {
		return timeMS > startTimeMS && timeMS < endTimeMS;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "";
		result += "Start Time: " + startTimeMS + " (ms)";
		result += "\nEnd Time  : " + endTimeMS + " (ms)";
		for(String s : features.keySet()) {
			result += "\n" + s + ": ";
			Object data = features.get(s);
			result += data;
		}
		return result;
	}
	
}