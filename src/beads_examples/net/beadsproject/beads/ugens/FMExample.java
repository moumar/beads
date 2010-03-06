package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.SineBuffer;

public class FMExample {
	
	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		//an envelope that defines the base frequency of the sound
		final Envelope baseFreqEnv = new Envelope(ac, 500f);
		//and an envelope to control the ratio of the modulator frequency
		final Envelope modFreqRatioEnv = new Envelope(ac, 1f);
		//and an envelope to control the ratio of the modulator frequency
		final Envelope modGainEnv = new Envelope(ac, 1000f);
		//create the modulator using the product of these two
		WavePlayer modulator = new WavePlayer(ac, new Function(baseFreqEnv, modFreqRatioEnv) {
			public float calculate() {
				return x[0] * x[1];
			}
		}, Buffer.SINE);
		//create the carrier using the base frequency, the mod gain and the modulator
		WavePlayer wp = new WavePlayer(ac, new Function(new UGen[] {baseFreqEnv, modGainEnv, modulator}) {
			@Override
			public float calculate() {
				return x[0] + x[1] * x[2];
			}
		}, Buffer.SINE);
		//do some stuff to the mod freq ratio
		modFreqRatioEnv.addSegment(2f, 10000f);
		modFreqRatioEnv.addSegment(3f, 1000f);
		modFreqRatioEnv.addSegment(1.5f, 1000f);
		modFreqRatioEnv.addSegment(0.5f, 1000f);
		//create a gain object
		Gain g = new Gain(ac, 1, 0.1f);
		g.addInput(wp);
		ac.out.addInput(g);
		ac.start();
	}

}
