/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

import java.util.ArrayList;
import java.util.List;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.core.UGen;

public abstract class AudioSegmenter extends UGen {
	
	/** The set of FeatureExtractors that respond to this Segmenter. */
	private ArrayList<FeatureExtractor<?, float[]>> listeners;
	
	/** The set of SegmentListener who are triggered by this Segmenter. */
	private List<SegmentListener> segmentListeners;
	
	/**
	 * Instantiates a new Segmenter.
	 * 
	 * @param context the AudioContext.
	 */
	public AudioSegmenter(AudioContext context) {
		super(context, 1, 0);		
		listeners = new ArrayList<FeatureExtractor<?, float[]>>();
		segmentListeners = new ArrayList<SegmentListener>();
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
	public void addSegmentListener(SegmentListener fr) {
		segmentListeners.add(fr);
	}

	/**
	 * Called by instantiations of Segmenter, to indicate that a new segment has been created. 
	 * 
	 * @param startTime double indicating the start time of the data chunk in milliseconds.
	 * @param endTime double indicating the end time of the data chunk in milliseconds.
	 * @param data the audio data.
	 */
	protected void segment(TimeStamp startTime, TimeStamp endTime, float[] data) {
		if(data != null) {
			for(FeatureExtractor<?, float[]> fe : listeners) {
				fe.process(startTime, endTime, data);
			}
		}
		for(SegmentListener recorder : segmentListeners) {
			recorder.newSegment(startTime, endTime);
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