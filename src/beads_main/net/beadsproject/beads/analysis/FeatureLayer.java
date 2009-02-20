package net.beadsproject.beads.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


/**
 * @author ollie
 *
 * Stores a set of features associated with a Sample (which may be loaded from a file or recorded in realtime).
 * 
 * A features can hold different views on the data. A list of Segments is maintained. Time-based features are stored in lists mapping segments to features.
 * 
 */
public class FeatureLayer implements Serializable, Iterable<FeatureFrame> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ArrayList<FeatureFrame> frames;
	
	public FeatureLayer() {
		frames = new ArrayList<FeatureFrame>();
	}
	
	public void add(FeatureFrame ff) {
		frames.add(ff);
	}
	
	public FeatureFrame get(int index) {
		return frames.get(index);
	}

	public FeatureFrame getFrameAt(double timeMS) {
		for(FeatureFrame ff : frames) {
			if(ff.containsTime(timeMS)) return ff;
		}
		return null;
	}

	public Iterator<FeatureFrame> iterator() {
		return frames.iterator();
	}
}
