package net.beadsproject.beads.play;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.beadsproject.beads.events.Pattern;

public class Music {
	
	/**
	 * A set of named lists of SoundEvents.
	 */
	public final static Map<String, List<SoundEvent>> kits = new Hashtable<String, List<SoundEvent>>();
	
	/**
	 * A set of named Patterns.
	 */
	public final static Map<String, Pattern> patterns = new Hashtable<String, Pattern>();
}
