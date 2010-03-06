package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.core.Envelope;
import net.beadsproject.beads.ugens.sample.SamplePlayer;


public class SamplePlayerPositionEnvelopeExample {
	public static void main(String[] args) {
	   	System.out.println("Testing: " + SamplePlayer.class);
		AudioContext ac = new AudioContext(512);
		Sample s1 = SampleManager.sample("audio/1234.aif");	
		System.out.println(s1.getLength());
	
		SamplePlayer sp = new SamplePlayer(ac, s1); 	
//		sp.setInterpolationType(SamplePlayer.InterpolationType.CUBIC);
		sp.setInterpolationType(SamplePlayer.InterpolationType.LINEAR);
	
		Envelope posEnv = new Envelope(ac, 0f) {
			public void calculateBuffer() {
				super.calculateBuffer();
				System.out.println(bufOut[0]);
			}
		};
		posEnv.addSegment(2000f, 2000f);
		posEnv.addSegment(4000f, 4000f);
		posEnv.addSegment(2000f, 1000f);
		
		sp.setPositionEnvelope(posEnv);
		
		ac.out.addInput(sp);
		ac.start();
	}
}
