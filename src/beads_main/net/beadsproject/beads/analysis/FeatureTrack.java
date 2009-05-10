/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import net.beadsproject.beads.data.Sample;

/**
 * Stores a set of features associated with a continuous period of audio data.
 * 
 * A FeatureTrack can hold different views on the data. Time-based features are stored in lists mapping segments to features.
 * 
 * @author ollie
 */
public class FeatureTrack implements Serializable, Iterable<FeatureFrame> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The list of FeatureFrames. */
	private ArrayList<FeatureFrame> frames;
	
	/** Optional {@link Sample} that this FeatureTrack refers to. */
	private Sample sample;
	
	/**
	 * Instantiates a new FeatureTrack.
	 */
	public FeatureTrack() {
		this(null);
	}
	
	/** Instantiates a new FeatureTrack referring to the given sample. 
	 * @param sample the Sample to refer to. 
	 */
	public FeatureTrack(Sample sample) {
		frames = new ArrayList<FeatureFrame>();
		this.sample = sample;
	}
	
	/**
	 * Adds the specified FeatureFrame.
	 * 
	 * @param ff the FeatureFrame.
	 */
	public void add(FeatureFrame ff) {
		frames.add(ff);
	}
	
	/**
	 * Gets the FeatureFrame at the given index.
	 * 
	 * @param index the index.
	 * 
	 * @return the FeatureFrame.
	 */
	public FeatureFrame get(int index) {
		return frames.get(index);
	}

	/**
	 * Gets the frame for the given offset, in milliseconds, into the FeatureLayer.
	 * 
	 * @param timeMS the millisecond offset.
	 * 
	 * @return the FeatureFrame at this time.
	 */
	public FeatureFrame getFrameAt(double timeMS) {
		for(FeatureFrame ff : frames) {
			if(ff.containsTime(timeMS)) return ff;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<FeatureFrame> iterator() {
		return frames.iterator();
	}
}
