/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import net.beadsproject.beads.core.TimeStamp;

/**
 * Stores a set of features associated with a continuous period of audio data.
 * 
 * A FeatureTrack can hold different views on the data. Time-based features are stored in lists mapping segments to features.
 * 
 * @author ollie
 */
public class FeatureTrack implements Serializable, Iterable<FeatureFrame>, SegmentListener {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The list of FeatureFrames. */
	private SortedSet<FeatureFrame> frames;

	/** The list of FeatureExtractors used to extract data. */
	private transient List<FeatureExtractor<?, ?>> extractors;
	
	/**
	 * Instantiates a new FeatureTrack.
	 */
	public FeatureTrack() {
		frames = new TreeSet<FeatureFrame>();
		extractors = new ArrayList<FeatureExtractor<?,?>>();
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
		if(index >= frames.size()) {
			return null;
		}
		int count = 0;
		FeatureFrame result = null;
		for(FeatureFrame ff : frames) {
			result = ff;
			if(count == index) break;
			count++;
		}
		return result;
	}

	/**
	 * Gets the frame for the given offset, in milliseconds, into the FeatureLayer.
	 * 
	 * @param timeMS the millisecond offset.
	 * 
	 * @return the FeatureFrame at this time.
	 */
	public FeatureFrame getFrameAt(double timeMS) {
		FeatureFrame targetFrame = new FeatureFrame(timeMS, timeMS);
		SortedSet<FeatureFrame> headSet = frames.headSet(targetFrame);
		FeatureFrame theFrame = headSet.last();
		if(theFrame.containsTime(timeMS)) return theFrame;
		else return null;
	}
	
	/**
	 * Gets the frame for the given offset, in milliseconds, into the FeatureLayer, or the last frame before then.
	 * 
	 * @param timeMS the millisecond offset.
	 * 
	 * @return the FeatureFrame at this time.
	 */
	public FeatureFrame getFrameBefore(double timeMS) {
		if(getNumberOfFrames() == 0) {
			return null;
		}
		FeatureFrame targetFrame = new FeatureFrame(timeMS, timeMS);
		SortedSet<FeatureFrame> headSet = frames.headSet(targetFrame);
		if(headSet.size() > 0) {
			return headSet.last();
		} else {
			return frames.first();
		}
	}
	
	/**
	 * Gets the last FeatureFrame in this FeatureTrack. 
	 * 
	 * @return the last FeatureFrame in this FeatureTrack.
	 */
	public FeatureFrame getLastFrame() {
		return frames.last();
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
	
	/**
	 * Tells this FeatureTrack to log a new FeatureFrame with the given startTime and endTime. The FeatureTrack
	 * will gather features from its various feature extractors at this point.
	 */
	public void newSegment(TimeStamp startTime, TimeStamp endTime) {
		FeatureFrame ff = new FeatureFrame(startTime.getTimeMS(), endTime.getTimeMS());
		for(FeatureExtractor<?, ?> e : extractors) {
			Object features = e.getFeatures();
			try {
				Method cloneMethod = features.getClass().getMethod("clone", new Class[] {});
				ff.add(e.getName(), cloneMethod.invoke(features, new Object[] {}));
			} catch (Exception e1) {
				//is this ugly or what? Any better ideas?
				if(features instanceof float[]) {
					ff.add(e.getName(), ((float[])features).clone());
				} else if(features instanceof int[]) {
					ff.add(e.getName(), ((int[])features).clone());
				} else if(features instanceof double[]) {
					ff.add(e.getName(), ((double[])features).clone());
				} else if(features instanceof byte[]) {
					ff.add(e.getName(), ((byte[])features).clone());
				} else if(features instanceof short[]) {
					ff.add(e.getName(), ((short[])features).clone());
				} else if(features instanceof long[]) {
					ff.add(e.getName(), ((long[])features).clone());
				} else if(features instanceof Object[]) {
					ff.add(e.getName(), ((Object[])features).clone());
				}  else if(features instanceof boolean[]) {
					ff.add(e.getName(), ((boolean[])features).clone());
				} else {
					e1.printStackTrace();
				}
			} 
		}
		add(ff);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<FeatureFrame> iterator() {
		return frames.iterator();
	}

	/**
	 * Returns the number of FeatureFrames stored in this FeatureTrack.
	 * 
	 * @return number of FeatureFrames.
	 */
	public int getNumberOfFrames() {
		return frames.size();
	}


}
