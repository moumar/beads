package net.beadsproject.beads.ugens;

import java.util.Random;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;


public class GlideExample {

	public static void main(String[] args) {
		System.out.println("Testing: " + Glide.class);
		Random rng = new Random();
		//create the AudioContext
		AudioContext ac = new AudioContext();
		//create a WavePlayer
		WavePlayer wp = new WavePlayer(ac, 500f + rng.nextFloat() * 500f, Buffer.SINE);
		//create a Glide
		final Glide g = new Glide(ac, 500f);
		//set the time it takes to do its gliding
		g.setGlideTime(1000f);
		//use the Glide to control the frequency of the WavePlayer
		wp.setFrequencyEnvelope(g);
		//connect the WavePlayer to the output, via a Gain
		Gain gain = new Gain(ac, 1, 0.1f);
		gain.addInput(wp);
		ac.out.addInput(gain);
		//create a Clock
		final Clock c = new Clock(ac, 500f);
		//attach the clock to the main output
		ac.out.addDependent(c);
		//make the clock respond to a custom message that modifies the Glide
		c.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				if(c.isBeat()) {
					g.setValue((float)Math.random() * 1000f + 220f);
				}
			}
			
		});
		//go
		ac.start();
	}
}
