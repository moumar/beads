package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;
import net.beadsproject.beads.data.Buffer;

public class UsingLPRezFilter {

	public static class Basic {
		public static void main(String[] args) {
			// Create our audio context.
			AudioContext ac = new AudioContext();

			// Start with a sawtooth wave.
			WavePlayer saw = new WavePlayer(ac, 220, Buffer.SAW);

			// Create a 2-channel resonant low-pass filter.
			// with cut-off frequency 300 and resonance .98.
			LPRezFilter lpr = new LPRezFilter(ac, 2, 300.0f, .98f);

			// Add the saw wave to the filter input.
			lpr.addInput(saw);

			// Scale everything down so it's not to loud by running it through a
			// Gain
			// with a gain factor of .2.
			Gain gain = new Gain(ac, 2, .2f);
			gain.addInput(lpr);

			// Output everything to the speakers
			ac.out.addInput(gain);

			// Start audio processing.
			// Notice it doesn't sound like a sawtooth wave...
			ac.start();
		}
	}

	public static class Moderate {
		public static void main(String[] args) {

			// Create our audio context.
			AudioContext ac = new AudioContext();

			// Start with a sawtooth wave.
			WavePlayer saw = new WavePlayer(ac, 220, Buffer.SAW);

			// Build a slow sine oscillator that will sweep the filter frequency
			// from 50 hz to 4005 hz
			WavePlayer sine = new WavePlayer(ac, 1f, Buffer.SINE);
			Function freq = new Function(sine) {
				public float calculate() {
					return x[0] * 2000f + 2050;
				}
			};

			// Create a 2-channel resonant low-pass filter.
			// with cut-off frequency 300 and resonance .98.
			LPRezFilter lpr = new LPRezFilter(ac, 2, freq, .98f);

			// Add the saw wave to the filter input.
			lpr.addInput(saw);

			// Scale everything down so it's not to loud by running it through a
			// Gain
			// with a gain factor of .2.
			Gain gain = new Gain(ac, 2, .2f);
			gain.addInput(lpr);

			// Output everything to the speakers
			ac.out.addInput(gain);

			// Start audio processing.
			ac.start();
		}
	}
}
