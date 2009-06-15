package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;


public class SimplestWavePlayerExample {
	public static void main(String[] args) {
		System.out.println("Testing: " + WavePlayer.class);
		AudioContext ac = new AudioContext();
		WavePlayer wp = new WavePlayer(ac, 440f, Buffer.SINE);
		Gain g = new Gain(ac, 2, 1f);
		g.addInput(wp);
		ac.out.addInput(g);
		ac.start();
	}

}
