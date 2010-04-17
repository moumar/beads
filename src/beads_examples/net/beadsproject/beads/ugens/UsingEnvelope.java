package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.AudioContextStopTrigger;

public class UsingEnvelope {

	public static class Basic {
		public static void main(String[] args) {
			/*
			 * Envelopes are UGens that generate sequences of line segments.
			 * They are usually used to control the parameters of another
			 * UGen. In this example we're using an Envelope to control
			 * the frequency of a WavePlayer, and another to control the 
			 * gain of a Gain.
			 */
			AudioContext ac = new AudioContext();
			/*
			 * Here's the frequency Envelope
			 */
			Envelope freqEnv = new Envelope(ac, 1000f);
			/*
			 * The WavePlayer can take a UGen in its constructor to
			 * specify what controls the frequency.
			 */
			WavePlayer wp = new WavePlayer(ac, freqEnv, Buffer.SINE);
			/*
			 * Likewise, this Envelope will control the Gain object.
			 */
			Envelope gainEnv = new Envelope(ac, 0f);
			Gain g = new Gain(ac, 1, gainEnv);
			g.addInput(wp);
			ac.out.addInput(g);
			/*
			 * Once the formalities are over. Give the Envelopes some 
			 * commands. Each segment is determined by a destination 
			 * value and a duration in milliseconds.
			 * 
			 * First the gain envelope.
			 */
			gainEnv.addSegment(0.2f, 500);
			gainEnv.addSegment(0.2f, 6000);
			/*
			 * Here the third argument is a trigger (any Bead can 
			 * be triggered). This trigger kills the program).
			 */
			gainEnv.addSegment(0, 1000, new AudioContextStopTrigger(ac));
			/*
			 * Then the frequency envelope.
			 * 
			 * Note that addSegment() returns immediately, none of the
			 * gainEnv segments have executed yet, we're still at t=0.
			 */
			freqEnv.addSegment(100, 1000);
			freqEnv.addSegment(10000, 3000);
			/*
			 * Here the third argument specifies a curvature. Numbers greater
			 * than 1 are accelerating, starting shallow and getting steeper towards
			 * the end of the curve, whereas numbers less than 1 (greater than 0)
			 * are decelerating, starting steep and getting shallower towards the
			 * end of the curve. 1 gives a linear curve.
			 */
			freqEnv.addSegment(500, 2000, 2);
			/*
			 * Tadaa
			 */
			for(int i = 0; i < 100; i++) {
				freqEnv.addSegment(1000, 50);
				freqEnv.addSegment(500, 50);
			}
			/*
			 * Start audio.
			 */
			ac.start();
		}
	}
}
