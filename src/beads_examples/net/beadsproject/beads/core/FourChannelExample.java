package net.beadsproject.beads.core;

import javax.sound.sampled.AudioFormat;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;


public class FourChannelExample {

	public static void main(String[] args) {
		
		//expect this to crash if you don't have 4 channels!
		final AudioContext ac = new AudioContext(AudioContext.DEFAULT_BUFFER_SIZE, 
											AudioContext.defaultAudioIO(),
											AudioContext.defaultAudioFormat(4));
		
		final Clock c = new Clock(ac, 500f);
		
		c.addMessageListener(new Bead() {
			private int count = 0;
			public void messageReceived(Bead message) {
				if(c.isBeat()) {
					WavePlayer wp = new WavePlayer(ac, (float)Math.random() * 1000f + 500f, Buffer.SINE);
					Envelope e = new Envelope(ac, 1f);
					Gain g = new Gain(ac, 1, e);
					e.addSegment(0f, 1000f, new KillTrigger(g));
					g.addInput(wp);
					ac.out.addInput(count++ % 4, g, 0);
				}
			}
		});
		
		ac.start();
		
	}
}
