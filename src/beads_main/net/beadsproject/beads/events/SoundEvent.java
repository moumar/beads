package net.beadsproject.beads.events;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.DataBead;

public interface SoundEvent {
	
	public UGen play(UGen output, DataBead parameters);
	
}
