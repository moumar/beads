package net.beadsproject.beads.ugens;

import java.util.Random;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.core.Envelope;
import net.beadsproject.beads.ugens.synth.WavePlayer;
import net.beadsproject.beads.ugens.utility.DelayTrigger;


public class EnvelopeExample {
	
	public static void main(String[] args) {
		System.out.println("Testing: " + Envelope.class);
		AudioContext ac = new AudioContext();
		WavePlayer wp = new WavePlayer(ac, 500f + 500f, new SineBuffer().getDefault());
		//create an envelope
		final Envelope e = new Envelope(ac, 500f);
		//give the envelope some tasks to do
		e.addSegment(100f, 1000f, 2f, null);
		e.addSegment(500f, 2000f, 0.5f, null);
		e.addSegment(20f, 3000f);
		e.addSegment(50f, 0f);
		e.addSegment(50f, 500f);
		e.addSegment(440f, 0f);
		e.addSegment(880f, 1000f);
		//now use the envelope to control the WavePlayer
		wp.setFrequencyEnvelope(e);
		ac.out.addInput(wp);
		ac.start();
	}
	
}
