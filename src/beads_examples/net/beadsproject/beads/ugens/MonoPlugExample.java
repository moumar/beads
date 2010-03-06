package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;

public class MonoPlugExample {
	
	public static void main(String[] args) {
		System.out.println("Testing: " + MonoPlug.class);
		AudioContext ac = new AudioContext();
		ac.start();
		WavePlayer wp1 = new WavePlayer(ac, 500f, new SineBuffer().getDefault());
		WavePlayer wp2 = new WavePlayer(ac, 600f, new SineBuffer().getDefault());
		Gain g = new Gain(ac, 2);
		g.addInput(0, wp1, 0);
		g.addInput(1, wp2, 0);
		
		//compare these...
		MonoPlug p1 = new MonoPlug(ac);
		p1.addInput(0, g, 0);
		MonoPlug p2 = new MonoPlug(ac);
		p2.addInput(0, g, 0);
		
		ac.out.addInput(p1);
	}
}
