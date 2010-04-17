package net.beadsproject.beads.core.io;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Reverb;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * To run this test make sure you have Jack running and that Java knows where
 * to find the correct native jjack library for your system.
 * <p/>
 * <p/> e.g., -Djava.library.path=lib/native/mac_x86_64
 * <p/> The autoconnect flag is also handy: -Djjack.ports.autoconnect=true
 * (otherwise you will have to connect JJack to the system once you get started).
 * 
 * @author ollie
 *
 */
public class JJackAudioIOTest {
	
	public static void main(String[] args) {
		JJackAudioIO io = new JJackAudioIO();
		AudioContext ac = new AudioContext(256, io);
		//some boring sound
		Gain g = new Gain(ac, 1, 0.5f);
		ac.out.addInput(g);
		WavePlayer wp = new WavePlayer(ac, 500f, Buffer.SINE);
		g.addInput(wp);
		//less boring, some audio inputs
		UGen in = ac.getAudioInput(new int[]{0});
		Reverb rb = new Reverb(ac);
		rb.addInput(in);
		ac.out.addInput(rb);
		//go
		ac.start();
	}
}
