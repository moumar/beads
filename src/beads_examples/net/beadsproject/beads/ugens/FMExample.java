package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.buffers.SineBuffer;

public class FMExample {
	
	public static void main(String[] args) {
		
		AudioContext ac = new AudioContext(512, 5000);
		final Envelope freqEnv = new Envelope(ac, 500f);
		Envelope gainEnv = new Envelope(ac, 0.1f);
		Gain g = new Gain(ac, 1, gainEnv);
		final Envelope lfoEnv =new Envelope(ac, 10f);
		lfoEnv.addSegment(100f, 10000f);
		WavePlayer lfo = new WavePlayer(ac, lfoEnv, new SineBuffer().getDefault());
		WavePlayer wp = new WavePlayer(ac, new Function(new UGen[] {freqEnv, lfo}) {
			@Override
			public float calculate() {
				return x[0] + 40f * x[1];
			}
		}, new SineBuffer().getDefault());
		freqEnv.addSegment(1000f, 1000f);
		freqEnv.addSegment(1000f, 1000f);
		freqEnv.addSegment(500f, 1000f);
		Clock c = new Clock(ac, 1000f);
		ac.out.addDependent(c);
		c.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				Clock c = (Clock)message;
				if(c.isBeat()) {
					System.out.println("tick");
					freqEnv.clear();
					freqEnv.addSegment((float)Math.random() * 4000f + 10f, c.getIntervalEnvelope().getValue());
				}
			}
		});
		g.addInput(wp);
		ac.out.addInput(g);
		ac.start();
	}

}
