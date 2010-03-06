/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.events;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.DataBead;

public interface SoundEvent {
	
	public UGen play(UGen output, DataBead parameters);
	
}
