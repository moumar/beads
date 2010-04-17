package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;

public class UsingWavePlayer {

	public static class Basic {
		public static void main(String[] args) {
			/*
			 * Very simple. WavePlayer is used in a number of
			 * other examples so look around to see more complex
			 * usage. For example, the UsingUGen example shows
			 * simple FM synthesis.
			 */
			AudioContext ac = new AudioContext();
			WavePlayer wp = new WavePlayer(ac, 500f, Buffer.SINE);
			Gain g = new Gain(ac, 1, 0.1f);
			g.addInput(wp);
			ac.out.addInput(g);
			ac.start();
		}
	}
}
