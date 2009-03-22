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
	private ArrayList<FeatureExtractor<?, float[]>> responders;
	
	/** The set of Beads that are triggered when this Segmenter segments. */
	private BeadArray listeners;

	/** The current start time in samples. */
	private long startTime;
	
	/** The current time in milliseconds since start. */
	protected double currentTime;

	/** The previous end time in milliseconds since start. */
	protected double previousEndTime;
	
	/**
	 * Instantiates a new Segmenter.
	 * 
	 * @param context the AudioContext.
	 */
	public Segmenter(AudioContext context) {
		super(context, 1, 0);		
		responders = new ArrayList<FeatureExtractor<?, float[]>>();
		listeners = new BeadArray();
		startTime();
		currentTime = previousEndTime = 0;
	}
	
	/**
	 * Sets the start time to now (now being determined by the AudioContext).
	 */
	public void startTime() {
		startTime = context.getTimeStep() * context.getBufferSize();
	}
	
	/**
	 * Adds a FeatureExtractor as a responder to this Segmenter.
	 * 
	 * @param fe the FeatureExtractor.
	 */
	public void addListener(FeatureExtractor<?, float[]> fe) {
		responders.add(fe);
	}
	
	/**
	 * Adds a Bead as a listener. Listeners are triggered whenever the {@link #segment(float[], int)} method is called.
	 * 
	 * @param bead the Bead.
	 */
	public void addListener(Bead bead) {
		listeners.add(bead);
	}
	
	/**
	 * Called by instantiations of Segmenter, to indicate that a new segment has been created. The caller must pass the segment data as well as an integer indicating the number of samples since the last segment.
	 * 
	 * @param data the audio data.
	 * @param length the number of samples since the previous data.
	 */
	protected void segment(float[] data, int sampleOffset) {
		for(FeatureExtractor<?, float[]> fe : responders) {
			fe.process(data);
		}
		currentTime = context.samplesToMs(context.getTimeStep() * context.getBufferSize() + sampleOffset - startTime);
		listeners.message(this);
		previousEndTime = currentTime;
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.Bead#toString()
	 */
	public String toString() {
		String result = "Segmenter: " + getClass().getSimpleName();
		for(FeatureExtractor<?, float[]> fe : responders) {
			result += "\n    " + fe.getName();
		}
		return result;
	}
	
}
