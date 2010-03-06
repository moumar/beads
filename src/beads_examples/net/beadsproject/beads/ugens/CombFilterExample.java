package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.SampleManager;

public class CombFilterExample {

	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		
		CombFilter cf = new CombFilter(ac, 10000);
		cf.setA(1f);
		cf.setG(1f);
		cf.addInput(sp);
		ac.out.addInput(cf);
		ac.start();
	}
}
