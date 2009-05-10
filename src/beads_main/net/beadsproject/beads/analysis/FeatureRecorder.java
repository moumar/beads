/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

import java.util.ArrayList;
import java.util.List;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.TimeStamp;

/**
 * A FeatureRecorder records feature data from a feature extraction process to a {@link FeatureTrack}. 
 * Typically, a FeatureRecorder is given a set of {@link FeatureExtractor}s to log data from, and is 
 * set to listen to a TimeStamped message from a {@link Bead}, which lets the FeatureRecorder know when the {@link FeatureExtractor}s 
 * have new data to log.
 */
public class FeatureRecorder extends Bead {

	/** The current track being recorded to. */
	private FeatureTrack currentTrack;
	
	/** The list of FeatureExtractors used to extract data. */
	private List<FeatureExtractor<?, ?>> extractors;
	
	/**
	 * Instantiates a new feature recorder.
	 */
	public FeatureRecorder() {
		currentTrack = new FeatureTrack();
		extractors = new ArrayList<FeatureExtractor<?, ?>>();
	}
	
	/**
	 * Adds a new FeatureExtractor.
	 * 
	 * @param e the FeatureExtractor.
	 */
	public void addFeatureExtractor(FeatureExtractor<?, ?> e) {
		extractors.add(e);
	}
	
	/**
	 * Removes the specified FeatureExtractor.
	 * 
	 * @param e the FeatureExtractor.
	 */
	public void removeFeatureExtractor(FeatureExtractor<?, ?> e) {
		extractors.remove(e);
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.Bead#messageReceived(net.beadsproject.beads.core.Bead)
	 */
	public void logFrame(double startTime, double endTime) {
		System.out.println("x");
		FeatureFrame ff = new FeatureFrame(startTime, endTime);
		for(FeatureExtractor<?, ?> e : extractors) {
			ff.add(e.getName(), e.getFeatures());
		}
		currentTrack.add(ff);
	}

	/**
	 * Sets the FeatureTrack.
	 * 
	 * @param ft the new FeatureTrack
	 */
	public void setFeatureTrack(FeatureTrack ft) {
		currentTrack = ft;
	}
	
	/**
	 * Gets the FeatureTrack.
	 * 
	 * @return the FeatureTrack.
	 */
	public FeatureTrack getFeatureTrack() {
		return currentTrack;
	}
	
	
}
