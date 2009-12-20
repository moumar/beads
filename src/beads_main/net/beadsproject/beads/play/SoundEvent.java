package net.beadsproject.beads.play;

import java.util.Map;

import net.beadsproject.beads.core.UGen;

public interface SoundEvent {
	
	public UGen play(UGen output, Map<String, Object> parameters);
	
}
