package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.AudioContextStopTrigger;

public class SamplePlayerCoarseVersusFine {

	public static void main(String[] args) {
		System.out.println("Compares COARSE versus FINE Sample Player speed.");		
		
		AudioContext ac = new AudioContext(512);
		Sample s1 = SampleManager.sample("audio/1234.aif");
		
		final SamplePlayer sp_coarse = new SamplePlayer(ac, s1);
		sp_coarse.setEnvelopeType(SamplePlayer.EnvelopeType.COARSE);
		sp_coarse.setKillListener(new AudioContextStopTrigger(ac));  
		
		final SamplePlayer sp_fine = new SamplePlayer(ac, s1);
		sp_fine.setEnvelopeType(SamplePlayer.EnvelopeType.FINE);
		sp_fine.setKillListener(new AudioContextStopTrigger(ac)); 
		
		/* record the time of each ugen */
		sp_coarse.setTimerMode(true);
		sp_fine.setTimerMode(true);
		Clock c = new Clock(ac,100.f);
		c.addMessageListener(new Bead()
		{
			double coarse_ms = 0;
			double fine_ms = 0;
			long steps = 0;
			
			public void messageReceived(Bead msg)
			{
				long coarse_t = sp_coarse.getTimeTakenLastUpdate();
				long fine_t = sp_fine.getTimeTakenLastUpdate();
				//if (((Clock)(msg)).isBeat())
				//	System.out.printf("(%d,%d),",coarse_t,fine_t);
				coarse_ms += (1./1000000)*coarse_t;
				fine_ms += (1./1000000)*fine_t;	
				steps++;
				
				if (((Clock)(msg)).isBeat())
					System.out.printf("(%f,%f),",coarse_ms/steps,fine_ms/steps);				
			}
		});
		
    	ac.out.addDependent(c);
    	ac.out.addInput(sp_coarse);
    	ac.out.addInput(sp_fine);
    	ac.start();
	}

}
