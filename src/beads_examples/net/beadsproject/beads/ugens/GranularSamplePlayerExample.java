package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.CosineWindow;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;

public class GranularSamplePlayerExample {

    public static void main(String[] args) throws Exception {
    	System.out.println("Testing: " + GranularSamplePlayer.class);
    	AudioContext ac = new AudioContext(512);
    	Sample s1 = SampleManager.sample("audio/1234.aif");	
    	System.out.println(s1.getLength());
    	GranularSamplePlayer gsp = new GranularSamplePlayer(ac, s1); 
    	
    	//could choose a different grain window
    	gsp.setWindow(new CosineWindow().getDefault());
    	
    	gsp.getGrainIntervalEnvelope().setValue(20f);
    	gsp.getGrainSizeEnvelope().setValue(50f);
    	Envelope rateEnv = new Envelope(ac, 1f);
    	rateEnv.addSegment(4f, 5000f);
    	gsp.setRateEnvelope(rateEnv);
    	Envelope pitchEnv = new Envelope(ac, 1f);
    	pitchEnv.addSegment(0.1f, 5000f);
    	gsp.setPitchEnvelope(pitchEnv);
    	gsp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING); 
    	gsp.getLoopEndEnvelope().setValue(1000f);
    	gsp.getLoopStartEnvelope().setValue(500f);  	
    	ac.out.addInput(gsp);
    	ac.start();
    }
    
}
