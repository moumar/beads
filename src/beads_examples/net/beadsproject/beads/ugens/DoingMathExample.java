package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.WavePlayer;


public class DoingMathExample {
	
	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		WavePlayer x = new WavePlayer(ac, 1f, new SineBuffer().getDefault());
		WavePlayer y = new WavePlayer(ac, 500f, new SineBuffer().getDefault());
		//rather than providing Max-style UGens for every operation, make a custom math op for your needs
		UGen g = new UGen(ac, 2, 1) {
			@Override
			public void calculateBuffer() {
				for(int i = 0; i < bufferSize; i++) {
					bufOut[0][i] = (0.5f + 0.2f * bufIn[0][i]) * bufIn[1][i];
				}
			}
		};
		//to make this a wee bit easier, use the Function UGen instead
		Function f = new Function(g) {
			public float calculate() {
				return x[0] * 2f;
			}
		};
		g.addInput(0, x, 0);
		g.addInput(1, y, 0);
		ac.out.addInput(f);
		ac.start();
	}

}
