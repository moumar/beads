package net.beadsproject.beads.ugens;

import java.util.Random;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.OscillatorBank;


public class OscillatorBankExample {
	
	public static void main(String[] args) {
		example1();
//		example2();
	}
	
	public static void example1() {
		System.out.println("Testing: " + OscillatorBank.class);
		Random rng = new Random();
		AudioContext ac = new AudioContext();
		int numOscs = 10;
		float[] freqs = new float[numOscs];
		for(int i = 0; i < numOscs; i++) {
			freqs[i] = rng.nextFloat() * 5000f + 100f;
		}
		OscillatorBank ob = new OscillatorBank(ac, new SineBuffer().getDefault(), numOscs);
		ob.setFrequencies(freqs);
		ac.out.addInput(ob);
		ac.start();
	}
	
	public static void example2() {
		AudioContext ac = new AudioContext(512, 5000);
		OscPlayer ob = new OscPlayer(ac, new SineBuffer().getDefault(), 50);
		Clock c = new Clock(ac, 1000f);
		ac.out.addDependent(c);
		c.addMessageListener(ob);
		ac.out.addInput(ob);
		ac.start();
	}
	
	private static class OscPlayer extends OscillatorBank {

		Random rng;
		
		public OscPlayer(AudioContext context, Buffer buffer, int numOscillators) {
			super(context, buffer, numOscillators);
			rng = new Random();
		}
		
		public void messageReceived(Bead message) {
			float freqs[] = new float[frequency.length];
			for(int i = 0; i < frequency.length; i++) {
				freqs[i] = rng.nextFloat() * 5000f + 100f;
			}
			setFrequencies(freqs);
			System.out.println(frequency.length);
		}
		
	}
	
}
