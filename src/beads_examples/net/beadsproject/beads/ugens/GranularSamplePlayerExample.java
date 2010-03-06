package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.CosineWindow;

public class GranularSamplePlayerExample {

    public static void main(String[] args) throws Exception {
    	System.out.println("Testing: " + GranularSamplePlayer.class);
    	//create the AudioContext
    	AudioContext ac = new AudioContext();
    	//create a Sample using SampleManager to handle loading
    	Sample s1 = SampleManager.sample("audio/1234.aif");	
    	System.out.println("Sample Length: " + s1.getLength());
    	//create the GranularSamplePlayer using this Sample to initialize it
    	GranularSamplePlayer gsp = new GranularSamplePlayer(ac, s1); 
    	//play with the settings of the GranularSamplePlayer
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
    	//connect and go
    	ac.out.addInput(gsp);
    	ac.start();
    }
    
}
