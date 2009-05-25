package net.beadsproject.beads.data;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;


public class PatternExample {
	
	/*
	 * A pretty example that plays some random sounds from a file, but also conveniently doubles
	 * up as a test case for Ben to debug the SampleManager timed buffering regimes.
	 * 
	 * Another TODO: when setting 'region size' Sample should not allow the region size to be bigger than the length of sample itself.
	 */

	public static void main(String[] args) {
		final AudioContext ac = new AudioContext();
		String dir = "/Users/ollie/Music/Audio/crash test audio/469-23_SpeedY_Nylon_Guitar_Single_notes.";
		
		SampleManager.setBufferingRegime(new Sample.TimedRegime(100, 0, 0, 50000, Sample.TimedRegime.Order.NEAREST));
		
		SampleManager.group("sounds", dir);
		final Clock c = new Clock(ac, 500);
		ac.out.addDependent(c);
		c.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				if(c.isBeat()) {
					SamplePlayer sp = new SamplePlayer(ac, SampleManager.randomFromGroup("sounds"));
					ac.out.addInput(sp);
				}
			}
		});
		ac.out.setValue(0.1f);
		ac.start();
	}
	
}
