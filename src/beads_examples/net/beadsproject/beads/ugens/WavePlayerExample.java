package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.core.Gain;
import net.beadsproject.beads.ugens.synth.WavePlayer;


public class WavePlayerExample {
	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		Gain g = new Gain(ac, 2, 1f);
		g.addInput(new WavePlayer(ac, 440f, Buffer.SINE));
		ac.out.addInput(g);
		ac.start();
	}

}
