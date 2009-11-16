package net.beadsproject.beads.play;

import java.util.Map;

import net.beadsproject.beads.core.UGen;

public interface SoundEvent {
	
	public void play(UGen output, Map<String, Object> parameters);
	
}
