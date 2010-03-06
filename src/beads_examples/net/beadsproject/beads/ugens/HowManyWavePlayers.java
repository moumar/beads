package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;


public class HowManyWavePlayers {

	//Try and thrash your system
	public final static int NUM_OSCS = 1000;
	
	public static void main(String[] args) {
		final AudioContext ac = new AudioContext();
		Gain g = new Gain(ac, 2);
		g.getGainEnvelope().setValue(0.5f / NUM_OSCS);
		for(int i = 0; i < NUM_OSCS; i++) {
			WavePlayer wp = new WavePlayer(ac, (float)Math.random() * 50f + 1000f, new SineBuffer().getDefault());
			g.addInput(wp);
		}
		ac.out.addInput(g);
		ac.start();
//		ac.out.update();
	}
}
