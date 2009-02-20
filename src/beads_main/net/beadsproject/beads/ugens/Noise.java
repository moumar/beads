package net.beadsproject.beads.ugens;

import java.util.Random;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;


public class Noise extends UGen {

	private Random rng;
	
	public Noise(AudioContext context) {
		super(context, 1);
		rng = new Random();
	}

	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			bufOut[0][i] = rng.nextFloat();
		}
	}

}
