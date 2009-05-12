package net.beadsproject.beads.analysis;

import net.beadsproject.beads.core.TimeStamp;


public interface SegmentListener {

	public void newSegment(TimeStamp start, TimeStamp end);
}
