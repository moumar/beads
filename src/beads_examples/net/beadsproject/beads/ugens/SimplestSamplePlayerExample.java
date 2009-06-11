package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;


public class SimplestSamplePlayerExample {
	public static void main(String[] args) {
		System.out.println("Testing: " + SamplePlayer.class);
		AudioContext ac = new AudioContext();
	   	Sample s1 = SampleManager.sample("audio/1234.aif");
    	SamplePlayer sp = new SamplePlayer(ac, s1);
    	Envelope rateEnvelope = new Envelope(ac, 1f);
    	rateEnvelope.addSegment(2f, 5000f);
    	sp.setRateEnvelope(rateEnvelope);
		Gain g = new Gain(ac, 1, 1f);
		g.addInput(sp);
		ac.out.addInput(g);
		ac.start();
	}
}
