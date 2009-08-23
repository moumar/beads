package net.beadsproject.beads.miscexperiments;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.Sample.Regime;
import net.beadsproject.beads.data.Sample.TimedRegime;
import net.beadsproject.beads.data.Sample.TimedRegime.Order;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.SamplePlayer;


public class PatternExample {
	
	/*
	 * A pretty example that plays some random sounds from a file, but also conveniently doubles
	 * up as a test case for Ben to debug the SampleManager timed buffering regimes.
	 * 
	 * ...which by the way are probably not working yet
	 * 
	 * Another TODO: when setting 'region size' Sample should not allow the region size to be bigger than the length of sample itself.
	 */

	public static void main(String[] args) {
		final AudioContext ac = new AudioContext();
		String dir = "/Users/ollie/Music/Audio/crash test audio/469-23_SpeedY_Nylon_Guitar_Single_notes.";
//		String dir = "D:/audio/crash test audio/469-23_SpeedY_Nylon_Guitar_Single_notes.";
		// String dir = "D:/audio/crash test audio/chopped live sounds/Bongos";
		
		// SampleManager.setBufferingRegime(new Sample.TimedRegime(100, 0, 0, 0, Sample.TimedRegime.Order.ORDERED));
		
		Sample.Regime r = new Sample.TimedRegime(1000, 0, 0, -1, Sample.TimedRegime.Order.ORDERED);
		r.storeInNativeBitDepth = true;
		SampleManager.setBufferingRegime(r);
		//SampleManager.setBufferingRegime(Sample.Regime.TOTAL);
		
		SampleManager.group("sounds", dir);
		final Clock c = new Clock(ac, 100);
		ac.out.addDependent(c);
		c.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				if(c.isBeat()) {
					// SimpleSamplePlayer sp = new SimpleSamplePlayer(ac, SampleManager.randomFromGroup("sounds"));
					SamplePlayer sp = new SamplePlayer(ac, SampleManager.randomFromGroup("sounds"));
					ac.out.addInput(sp);
				}
			}
		});
		ac.out.setValue(0.1f);
		ac.start();
	}
	
}
