package net.beadsproject.beads.core;

import net.beadsproject.beads.core.AudioContext;

public class AudioContextExample {
	
	public static void main(String[] args) {
		AudioContext.printMixerInfo();
		AudioContext ac = new AudioContext(512);
		ac.logTime(true);
		ac.start();
	}
	
}
