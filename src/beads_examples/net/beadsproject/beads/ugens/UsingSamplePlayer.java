package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer.LoopType;

public class UsingSamplePlayer {

	public static class Basics {
		public static void main(String[] args) {
			/*
			 * This is simple enough.
			 */
			AudioContext ac = new AudioContext();
			SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
			ac.out.addInput(sp);
			ac.start();
		}
	}
	
	public static class ControllingParameters {
		public static void main(String[] args) {
			AudioContext ac = new AudioContext();
			SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
			/*
			 * Rewind!
			 */
			Envelope rateEnv = new Envelope(ac, 1f);
			rateEnv.addSegment(1f, 5000f);
			rateEnv.addSegment(-10f, 5000f);
			sp.setRate(rateEnv);
			//
			ac.out.addInput(sp);
			ac.start();
		}
	}
	
	public static class Loops {
		public static void main(String[] args) {
			AudioContext ac = new AudioContext();
			SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
			/*
			 * Choose a loop type.
			 */
			sp.setLoopType(LoopType.LOOP_ALTERNATING);
			/*
			 * Set the loop points.
			 */
			sp.getLoopStartUGen().setValue(200f);
			sp.getLoopEndUGen().setValue(500f);
			//
			ac.out.addInput(sp);
			ac.start();
		}
	}
}
