package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;
import net.beadsproject.beads.data.*;

public class UsingGain {

	public static class Basic {
		public static void main(String[] args) {

			// Gain multiplies a channel or channels by a scalar value.
			// The scalar may be defined by a static float value or by a UGen.

			// Create our audio context
			AudioContext ac = new AudioContext();

			// Start with some white noise.
			Noise noise = new Noise(ac);
			
			// But lets scale that by a factor of .2 so it's not so loud.
			// We'll create a 1-channel Gain with a gain amount of .2.
			Gain gain = new Gain(ac, 1, .2f);
			
			// We'll add the noise to the Gain's input.
			gain.addInput(noise);
			
			// And add our Gain to the audio output.
			ac.out.addInput(gain);
			
			// And finally, start audio processing.
			ac.start();
		}
	}

	public static class Moderate {
		public static void main(String[] args) {
			
			// Gain multiplies a channel or channels by a scalar value.
			// The scalar may be defined by a static float value or by a UGen.

			// Create our audio context
			AudioContext ac = new AudioContext();

			// In this case, we'll create two sine tones of different
			// frequencies, one for each stereo channel.
			WavePlayer sineLeft = new WavePlayer(ac, 220, Buffer.SINE);
			WavePlayer sineRight = new WavePlayer(ac, 330, Buffer.SINE);

			// Next, we'll create a slow sine signal oscillating between 0 and .2
			// to serve as a scalar.
			WavePlayer slowSine = new WavePlayer(ac, .6f, Buffer.SINE);
			Function gainAmount = new Function(slowSine) {
				public float calculate() {
					return (x[0] + 1) * .1f;
				}
			};
			
			// Now we'll create our 2-channel Gain, using gainAmount as the scalar.
			Gain gain = new Gain(ac, 2, gainAmount);
			
			// We'll add each sine tone to its corresponding channel of the Gain.
			gain.addInput(0, sineLeft, 0);
			gain.addInput(1, sineRight, 0);
			
			// Add our Gain to the audio output.
			ac.out.addInput(gain);
			
			// And finally, start audio processing.
			ac.start();
		}
	}

}
