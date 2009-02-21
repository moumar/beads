/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

import java.util.ArrayList;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.UGen;

/**
 * A Segmenter is an abstract base class for classes that divide a stream of audio data into segments. Whenever a segment is created, using the method {@link #segment(float[], int)}, it is passed on to any {@link FeatureExtractor}s that are registered as responders to this Segmenter. Other {@link Bead}s can be added as listeners, that are triggered when {@link #segment(float[], int)} is called.
 * 
 * An implementation of a Segmenter must implement the method {@link UGen#calculateBuffer()}, and determine when {@link #segment(float[], int)} should be called, passing the audio data accordingly (this may require the Segmenter to record audio data).
 * 
 * In addition, the Segmenter keeps track of analysis data in a {@link FeatureLayer} for any {@link FeatureExtractor}s registered as extractors with this Segmenter.
 *  
 * <p/>NOTE: It is critical that the second argument passed to the method {@link #segment(float[], int)} correctly indicates the length since the previous segment.
 *  
 *  @author ollie
 */
public abstract class Segmenter extends UGen {
	
	//TODO use of currentTime and previousTime are broken. Reconsider design of segment method.

	/** The FeatureLayer to which analysis data is stored. */
	private FeatureLayer fl;
	
	/** The set of FeatureExtractors that respond to this Segmenter. */
	private ArrayList<FeatureExtractor> responders;
	
	/** The set of Beads that are triggered when this Segmenter segments. */
	private BeadArray listeners;
	
	/** The set of FeatureExtractors whose feature data is recorded by this Segmenter. */
	private ArrayList<FeatureExtractor> extractors;
	
	/** The current time in milliseconds. */
	protected double currentTime;
	
	protected double previousEndTime;
	
	/**
	 * Instantiates a new Segmenter.
	 * 
	 * @param context the AudioContext.
	 */
	public Segmenter(AudioContext context) {
		super(context, 1, 0);		
		responders = new ArrayList<FeatureExtractor>();
		extractors = new ArrayList<FeatureExtractor>();
		listeners = new BeadArray();
		setTime(0);
	}
	
	/**
	 * Sets the current time.
	 * 
	 * @param time the new time.
	 */
	public void setTime(double time) {
		currentTime = previousEndTime = time;
	}

	/**
	 * Sets the FeatureLayer.
	 * 
	 * @param fl the new FeatureLayer.
	 */
	public void setFeatureLayer(FeatureLayer fl) {
		this.fl = fl;
	}
	
	/**
	 * Gets the FeatureLayer.
	 * 
	 * @return the FeatureLayer.
	 */
	public FeatureLayer getFeatureLayer() {
		return fl;
	}
	
	/**
	 * Unsets the FeatureLayer.
	 */
	public void unsetFeatureLayer() {
		fl = null;
	}
	
	/**
	 * Adds a FeatureExtractor as a responder to this Segmenter.
	 * 
	 * @param fe the FeatureExtractor.
	 */
	public void addResponder(FeatureExtractor fe) {
		responders.add(fe);
	}
	
	/**
	 * Adds a FeatureExtractor to the list of FeatureExtractors whose data is recorded.
	 * 
	 * @param fe the FeatureExtractor.
	 */
	public void addExtractor(FeatureExtractor fe) {
		extractors.add(fe);
	}
	
	/**
	 * Adds a FeatureExtractor both as a responder to this Segmenter and to the list of FeatureExtractors whose data is recorded.
	 * 
	 * @param fe the FeatureExtractor.
	 */
	public void addResponderExtractor(FeatureExtractor fe) {
		addResponder(fe);
		addExtractor(fe);
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
	protected void segment(float[] data, int length) {
		for(FeatureExtractor fe : responders) {
			fe.process(data, length);
		}
		if(fl != null) {
			FeatureFrame ff = new FeatureFrame(previousEndTime, currentTime);
			for(FeatureExtractor fe : extractors) {
				ff.add(fe.getName(), fe.getFeatures());
			}
			fl.add(ff);
			previousEndTime = currentTime;
			currentTime += context.samplesToMs(length);
		}
		listeners.message(this);
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.Bead#toString()
	 */
	public String toString() {
		String result = "Segmenter: " + getClass().getSimpleName();
		for(FeatureExtractor fe : extractors) {
			result += "\n    " + fe.getName();
		}
		return result;
	}
	
}
