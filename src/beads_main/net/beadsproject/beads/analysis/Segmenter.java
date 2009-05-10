/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

import java.util.ArrayList;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.UGen;

public abstract class Segmenter extends UGen {
	
	/** The set of FeatureExtractors that respond to this Segmenter. */
	private ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	/** The set of FeatureRecorders whose recording is triggered by this Segmenter. */
	private ArrayList<FeatureRecorder> recorders;
	
	/**
	 * Instantiates a new Segmenter.
	 * 
	 * @param context the AudioContext.
	 */
	public Segmenter(AudioContext context) {
		super(context, 1, 0);		
		listeners = new ArrayList<FeatureExtractor<?, float[]>>();
		recorders = new ArrayList<FeatureRecorder>();
	}
	
	/**
	 * Adds a FeatureExtractor as a responder to this Segmenter.
	 * 
	 * @param fe the FeatureExtractor.
	 */
	public void addListener(FeatureExtractor<?, float[]> fe) {
		listeners.add(fe);
	}
	
	/**
	 * Adds a FeatureRecorder as a listener to this Segmenter.
	 * 
	 * @param fe the FeatureExtractor.
	 */
	public void addRecorder(FeatureRecorder fr) {
		recorders.add(fr);
	}

	/**
	 * Called by instantiations of Segmenter, to indicate that a new segment has been created. 
	 * 
	 * @param startTime double indicating the start time of the data chunk in milliseconds.
	 * @param endTime double indicating the end time of the data chunk in milliseconds.
	 * @param data the audio data.
	 */
	protected void segment(double startTime, double endTime, float[] data) {
		for(FeatureExtractor<?, float[]> fe : listeners) {
			fe.process(data);
		}
		for(FeatureRecorder recorder : recorders) {
			recorder.logFrame(startTime, endTime);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.Bead#toString()
	 */
	public String toString() {
		String result = "Segmenter: " + getClass().getSimpleName();
		for(FeatureExtractor<?, float[]> fe : listeners) {
			result += "\n    " + fe.getName();
		}
		return result;
	}
	
}
