package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.SampleManager;

public class UsingSamplePlayer {

	public static class Basics {
		public static void main(String[] args) {
			AudioContext ac = new AudioContext();
			SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
			ac.out.addInput(sp);
			ac.start();
		}
	}
}
