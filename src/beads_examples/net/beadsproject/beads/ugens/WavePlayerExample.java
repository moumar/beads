package net.beadsproject.beads.ugens;

import java.util.Random;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;

public class WavePlayerExample {
	
	public static void main(String[] args) {
		System.out.println("Testing: " + WavePlayer.class);
		Random rng = new Random();
		AudioContext ac = new AudioContext();
		WavePlayer wp = new WavePlayer(ac, 500f + rng.nextFloat() * 500f, Buffer.SINE);
		Envelope e = new Envelope(ac, 500f);
		e.addSegment(100f, 1000f, 2f, null);
		e.addSegment(500f + rng.nextFloat() * 500f, 2000f, 0.5f, null);
		wp.setFrequencyEnvelope(e);
		ac.out.addInput(wp);
		ac.start();
	}

}
