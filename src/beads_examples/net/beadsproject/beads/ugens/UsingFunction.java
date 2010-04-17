package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.CosineWindow;

public class UsingFunction {

	public static class Basic {
		public static void main(String[] args) {
			/*
			 * Function is just a quick and dirty way to
			 * put an equation into a signal chain. It is almost
			 * definitely not the most efficient way to do things 
			 * because it calls its calculate() function every
			 * time step. But it's super-easy.
			 */
			AudioContext ac = new AudioContext();
			/*
			 * A normal sound.
			 */
			WavePlayer sound = new WavePlayer(ac, 50, Buffer.SAW);
			/*
			 * A window to control the sound. This is a normal
			 * WavePlayer that uses a CosineWindow as its Buffer.
			 * The CosineWindow ranges from 0 to 1.
			 */
			WavePlayer mod = new WavePlayer(ac, 0.8f, new CosineWindow().getDefault());
			/*
			 * The Function takes any relevant incoming UGens as
			 * args (it is only concerned with single output UGens).
			 * 
			 * Then, within the calculate() method, these args
			 * are rendered as elements in an array, x[0], x[1], etc.
			 * 
			 * You just need to return the result of whatever you
			 * do with the x's.
			 */
			Function f = new Function(sound, mod) {
				@Override
				public float calculate() {
					return x[0] * (x[1] * 0.5f + 0.5f) * 0.1f;
				}
			};
			/*
			 * Geronimo.
			 */
			ac.out.addInput(f);
			ac.start();
		}
	}
}
