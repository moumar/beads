package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;

public class UsingOnePoleFilter {
	
	public static class Basic {
		public static void main(String[] args) {

			// The simplest of IIR filters: can be used to just take a little out of the high-end.
			
			// Create our audio context.
			AudioContext ac = new AudioContext();
			
			// Start with white noise.
			Noise n = new Noise(ac);
			
			// Create a one-pole low-pass filter with a cut-off frequency of 1800Hz.
			// Try changing the cut-off frequency for different frequency spectra.
			OnePoleFilter opf = new OnePoleFilter(ac, 1800);
			
			// Add the noise to the filter.
			opf.addInput(n);
			
			// Send the output to audio out.
			ac.out.addInput(opf);
			
			// Start audio processing.
			ac.start();
			
		}
	}
	
	public static class Moderate {
		public static void main(String[] args) {

			// The simplest of IIR filters: can be used to just take a little out of the high-end.
			
			// Create our audio context.
			AudioContext ac = new AudioContext();
			
			// Start with white noise.
			Noise n = new Noise(ac);
			
			// Create a cut-off frequency envelope for our filter with a starting value of 1.
			Envelope freq = new Envelope(ac, 1);
			
			// Create a one-pole low-pass filter with a cut-off frequency set by the envelope.
			OnePoleFilter opf = new OnePoleFilter(ac, freq);
			
			// Add the noise to the filter.
			opf.addInput(n);
			
			// Program some ramps for the cut-off frequency.
			freq.addSegment(100, 1000);
			freq.addSegment(100, 500);
			freq.addSegment(1, 700);
			freq.addSegment(1, 500);
			freq.addSegment(1000, 500);
			freq.addSegment(1000, 500);
			freq.addSegment(1, 400);
			freq.addSegment(10000, 300);
			freq.addSegment(10000, 500);
			freq.addSegment(1, 200);
			freq.addSegment(20000, 10000);		
			
			// Send the output to audio out.
			ac.out.addInput(opf);
			
			// Start audio processing.
			ac.start();
			
		}
	}


}
