package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.AudioContextStopTrigger;

public class SamplePlayerCoarseVersusFine {

	public static void main(String[] args) {
		System.out.println("Compares COARSE versus FINE Sample Player speed.");		
		
		AudioContext ac = new AudioContext(512);
		Sample s1 = SampleManager.sample("audio/1234.aif");
		
		SamplePlayer sp_coarse = new SamplePlayer(ac, s1);
		sp_coarse.setEnvelopeType(SamplePlayer.EnvelopeType.COARSE);
		sp_coarse.setKillListener(new AudioContextStopTrigger(ac));  
		
		SamplePlayer sp_fine = new SamplePlayer(ac, s1);
		sp_fine.setEnvelopeType(SamplePlayer.EnvelopeType.FINE);
		sp_fine.setKillListener(new AudioContextStopTrigger(ac)); 
		
		/* construct a new bead that times each ugen above it */
		
    	
    	ac.out.addInput(sp_coarse);
    	ac.start();
	}

}
