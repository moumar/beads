package net.beadsproject.beads.ugens;

import java.util.Random;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;


public class EnvelopeExample {
	
	public static void main(String[] args) {
		System.out.println("Testing: " + Envelope.class);
		Random rng = new Random();
		AudioContext ac = new AudioContext(512, 5000);
		WavePlayer wp = new WavePlayer(ac, 500f + rng.nextFloat() * 500f, new SineBuffer().getDefault());
		final Envelope e = new Envelope(ac, 500f);
		e.addSegment(100f, 1000f, 2f, null);
		e.addSegment(500f + rng.nextFloat() * 500f, 2000f, 0.5f, null);
		wp.setFrequencyEnvelope(e);
		ac.out.addInput(wp);
		ac.start();
		//then some more
		DelayTrigger dt = new DelayTrigger(ac, 10000f, new Bead() {
			public void messageReceived(Bead message) {
				System.out.println("message");
				e.addSegment(20f, 3000f);
				e.addSegment(50f, 0f);
				e.addSegment(50f, 500f);
				e.addSegment(440f, 0f);
				e.addSegment(880f, 1000f);
			}
		});
		ac.out.addDependent(dt);
	}
	
}
