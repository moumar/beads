package net.beadsproject.beads.events;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class UsingTriggers {

	public static class Basic {
		public static void main(String[] args) {
			/*
			 * Here's a common use of Triggers you've 
			 * probably seen elsewhere already.
			 * 
			 * Set some stuff up. An Envelope controlling
			 * the frequency of a WavePlayer, which is being
			 * attenuated by a Gain. See elsewhere if you
			 * don't know what this is all about yet.
			 */
			AudioContext ac = new AudioContext();
			Envelope freqEnv = new Envelope(ac, 300f);
			WavePlayer wp = new WavePlayer(ac, freqEnv, Buffer.SAW);
			Gain g = new Gain(ac, 1, 0.1f);
			g.addInput(wp);
			ac.out.addInput(g);
			/*
			 * Here's the critical bit. Then Envelope triggers
			 * a KillTrigger when it completes the following 
			 * Segment. The KillTrigger is assigned to kill the
			 * Gain object g, which means that the sound stops.
			 * 
			 * Not that Envelope, Gain and KillTrigger are all 
			 * instances of Bead. Bead can be passed messages,
			 * can be passed as a message, can kill and
			 * can be killed. In Beads, everything is a Bead.
			 * It's a little bit sick, really.
			 */
			freqEnv.addSegment(50, 1000, new KillTrigger(g));
			ac.start();
			/*
			 * Notice that it is only the Gain that has been killed.
			 * So AudioContext is still running. 
			 * 
			 * That is, until...
			 */
			DelayTrigger dt = new DelayTrigger(ac, 2000, new AudioContextStopTrigger(ac));
			ac.out.addDependent(dt);
		}
	}
}
