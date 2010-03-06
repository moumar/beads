package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.sample.GranularSamplePlayer;

public class SimpleGranularExample {

    public static void main(String[] args) throws Exception {
    	System.out.println("Testing: " + GranularSamplePlayer.class);
    	AudioContext ac = new AudioContext();
    	Sample s1 = SampleManager.sample("audio/1234.aif");	
    	System.out.println(s1.getLength());
    	GranularSamplePlayer gsp = new GranularSamplePlayer(ac, s1); 

    	gsp.getRateEnvelope().setValue(0.5f);
    	gsp.getPitchEnvelope().setValue(2f);
    	gsp.getGrainSizeEnvelope().setValue(50f);
    	gsp.getGrainIntervalEnvelope().setValue(20f);
    	gsp.getRandomnessEnvelope().setValue(0.1f);
    	
    	ac.out.addInput(gsp);
    	ac.start();
    }
}
