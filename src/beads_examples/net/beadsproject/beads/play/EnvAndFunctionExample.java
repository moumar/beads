package net.beadsproject.beads.play;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;


public class EnvAndFunctionExample {

	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		final Clock clock = new Clock(ac);
		final Envelope slider = new Envelope(ac, 175f);
		UGen tempoToInterval = new UGen(clock.getContext(), 1, 1) {
			@Override
			public void calculateBuffer() {
				for(int i = 0; i < bufferSize; i++) {
					bufOut[0][i] = 60000f / bufIn[0][i];
				}
			}
		};
		tempoToInterval.addInput(slider);
		clock.setIntervalEnvelope(tempoToInterval);
		ac.out.addDependent(clock);
		ac.start();
	}
}
