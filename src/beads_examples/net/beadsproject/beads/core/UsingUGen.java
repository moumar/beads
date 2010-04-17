package net.beadsproject.beads.core;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;


public class UsingUGen {

	public static class SuperSimple {
		public static void main(String[] args) {
			AudioContext ac = new AudioContext();
			/*
			 * There are many kinds of UGens.
			 * 
			 * WavePlayer is a kind of UGen that plays a buffer
			 * in a loop. It has no inputs and one output.
			 */
			WavePlayer wave = new WavePlayer(ac, 500f, Buffer.SINE);
			/*
			 * Noise is a kind of UGen that makes white noise. It
			 * has no inputs and one output.
			 */
			Noise noise = new Noise(ac);
			/*
			 * Gain is a kind of UGen that modifies the gain of a
			 * signal. It has N inputs and N outputs, where N is
			 * specified in the constructor.
			 */
			Gain g = new Gain(ac, 1, 0.2f);
			/*
			 * A UGen that has inputs can connect other UGens to those
			 * inputs. When multiple UGens are connected to the
			 * same input their signals are added together.
			 */
			g.addInput(wave);
			g.addInput(noise);
			/*
			 * The AudioContext gives you access to the outside world.
			 * Use ac.out, which is also a Gain object.
			 */
			ac.out.addInput(g);
			ac.start();
		}
	}
	
	public static class OnTheFlyUGen {
		public static void main(String[] args) {
			/*
			 * In this example a UGen is made on the fly
			 * to handle the mapping from a modulator signal
			 * to a carrier signal in a simple FM synth.
			 * 
			 * NOTE: The class Function is slightly more convenient
			 * for making classes on the fly. This example is just
			 * to illustrate the use of UGen.
			 */
			AudioContext ac = new AudioContext();
			/*
			 * Here is the modulator signal, it is a UGen too
			 * of the type WavePlayer. 
			 */
			WavePlayer modulator = new WavePlayer(ac, 10f, Buffer.SINE);
			/*
			 * Now we create a custom UGen with one input and one output.
			 */
			UGen modUGen = new UGen(ac, 1, 1) {
				/*
				 * Try overriding the calculateBuffer() method to tell your
				 * UGen how to handle audio.
				 */
				@Override
				public void calculateBuffer() {
					for(int i = 0; i < bufferSize; i++) {
						for(int j = 0; j < ins; j++) {
							/*
							 * In this case we're just taking the
							 * input (a sine wave) and mapping to suitable
							 * values to modulate the frequency. 500 is the
							 * base frequency and 10 is the modulation strength.
							 */
							bufOut[j][i] = bufIn[j][i] * 10f + 500f;
						}
					}
				}
			};
			/*
			 * Make the new UGen listen to the modulation signal.
			 */
			modUGen.addInput(modulator);
			/*
			 * Now we create the carrier signal, which is the sine wave
			 * that gets modulated by the custom UGen. WavePlayer can take
			 * a UGen argument to specify how the frequency of the wave gets
			 * modified.
			 */
			WavePlayer carrier = new WavePlayer(ac, modUGen, Buffer.SINE);
			/*
			 * Tally ho.
			 */
			Gain g = new Gain(ac, 1, 0.2f);
			g.addInput(carrier);
			ac.out.addInput(g);
			ac.start();
		}
	}
}
