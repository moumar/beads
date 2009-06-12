package net.beadsproject.beads.ugens;

import java.util.Random;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.buffers.SineBuffer;


public class GlideExample {

	public static void main(String[] args) {
		System.out.println("Testing: " + Glide.class);
		Random rng = new Random();
		AudioContext ac = new AudioContext();
		WavePlayer wp = new WavePlayer(ac, 500f + rng.nextFloat() * 500f, new SineBuffer().getDefault());
		final Glide g = new Glide(ac, 500f);
		g.setGlideTime(1000f);
		wp.setFrequencyEnvelope(g);
		ac.out.addInput(wp);
		final Clock c = new Clock(ac, 2000f);
		c.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				if(c.isBeat()) {
					g.setValue((float)Math.random() * 1000f + 1000f);
				}
			}
		});
		ac.out.addDependent(c);
		ac.start();
	}
}
