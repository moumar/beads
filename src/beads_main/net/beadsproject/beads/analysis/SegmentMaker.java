package net.beadsproject.beads.analysis;

/**
 * SegmentMakers notify {@link SegmentListener}s of new segments.
 */
public interface SegmentMaker {

	/**
	 * Adds the {@link SegmentListener}.
	 * 
	 * @param sl the segment listener.
	 */
	public void addSegmentListener(SegmentListener sl);
	
	/**
	 * Removes the {@link SegmentListener}.
	 * 
	 * @param sl the segment listener.
	 */
	public void removeSegmentListener(SegmentListener sl);
}
