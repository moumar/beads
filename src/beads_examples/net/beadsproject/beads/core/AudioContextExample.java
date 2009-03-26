package net.beadsproject.beads.core;

import net.beadsproject.beads.core.AudioContext;

public class AudioContextExample {
	
	public static void main(String[] args) {
		AudioContext ac = new AudioContext(512);
		ac.chooseMixerCommandLine();
		ac.logTime(true);
		ac.start();
	}
	
}
