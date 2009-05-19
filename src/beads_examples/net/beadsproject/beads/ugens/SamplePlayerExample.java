package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;

public class SamplePlayerExample {

    public static void main(String[] args) throws Exception {
    	System.out.println("Testing: " + SamplePlayer.class);
    	AudioContext ac = new AudioContext(512);
    	Sample s1 = SampleManager.sample("audio/1234.aif");	
    	System.out.println(s1.getLength());
    	
    	
    	SamplePlayer sp = new SamplePlayer(ac, s1); 	
    	Envelope rateEnv = new Envelope(ac, 1f);
    	rateEnv.addSegment(4f, 5000f);
//    	sp.setRateEnvelope(rateEnv);
    	sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING); 
    	sp.getLoopEndEnvelope().setValue(1000f);
    	sp.getLoopStartEnvelope().setValue(500f);  	
    	ac.out.addInput(sp);
    	ac.start();
    }
}
