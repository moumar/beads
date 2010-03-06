package net.beadsproject.beads.core;

import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;

public class UsingAudioContext {

	public static class MostBasic {
		public static void main(String[] args) {
			/*
			 * Audio context has many constructors to allow you to set 
			 * the buffer size, IO and AudioFormat.
			 * This gives you the default AudioContext, using JavaSound.
			 */
			AudioContext ac = new AudioContext();
			//make some sound
			Noise n = new Noise(ac);
			Gain g = new Gain(ac, 1, 0.05f);
			g.addInput(n);
			ac.out.addInput(g);
			//the AudioContext must be started
			ac.start();
		}
	}
	
	public static class NonRealTime {
		public static void main(String[] args) {
			//To run something in non-realtime, just 
			AudioContext ac = new AudioContext();
		}
	}
	
}
