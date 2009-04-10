package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;


public class HowManyWavePlayers {

	//Try and thrash your system
	public final static int NUM_OSCS = 100;
	public final static int BUFFER_SIZE = 512;
	public final static int IO_BUFFER_SIZE = 10000;
	
	public static void main(String[] args) {
		final AudioContext ac = new AudioContext(BUFFER_SIZE, IO_BUFFER_SIZE * 4);
		final Gain g = new Gain(ac, 2, 0.5f / NUM_OSCS);
		for(int i = 0; i < NUM_OSCS; i++) {
			WavePlayer wp = new WavePlayer(ac, (float)Math.random() * 5000f + 100f, new SineBuffer().getDefault());
			g.addInput(wp);
		}
		ac.out.addInput(g);
		
//		ac.out.addInput(new RTInput(ac));
		
//		ac.checkForDroppedFrames(false);
		ac.start();
	}
}
