package net.beadsproject.beads.miscexperiments;

import net.beadsproject.beads.analysis.AudioAnalyser;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;

public class AudioAnalyserExample {
	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		AudioAnalyser aa = new AudioAnalyser(ac);
		aa.addInput(sp);	
		
		PeakDetector pd = aa.peakDetector();
		pd.setThreshold(0.1f);
		pd.setAlpha(.9f);		
		pd.addMessageListener(new Bead()
		{
			public void messageReceived(Bead msg)
			{
				System.out.println(((PeakDetector)msg).getLastOnsetValue());
			}
		});
		
		ac.out.addInput(sp);		
		ac.start();
	}

}
