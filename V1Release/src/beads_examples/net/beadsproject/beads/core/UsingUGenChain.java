package net.beadsproject.beads.core;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;
import net.beadsproject.beads.ugens.WavePlayer;

public class UsingUGenChain {

	public static class BasicDelayExample {
		public static void main(String[] args) {
			final AudioContext ac = new AudioContext();
			//make a new UGenChain inline
			class Delay extends UGenChain {
				/*
				 * This is a delay with feedback, it uses a combination
				 * of Gains and Tap delays to achieve this.
				 */
				public Delay(AudioContext ac) {
					super(ac, 1, 1);
					//set up the chain
					Gain dryLevel = new Gain(ac, 1, 0.5f);
					drawFromChainInput(dryLevel);
					addToChainOutput(dryLevel);
					TapIn tin = new TapIn(ac, 10000);
					drawFromChainInput(tin);
					TapOut tout = new TapOut(ac, tin, 50);
					Gain wetLevel = new Gain(ac, 1, 0.3f);
					wetLevel.addInput(tout);
					addToChainOutput(wetLevel);
					Gain feedbackLevel = new Gain(ac, 1, 0.9f);
					feedbackLevel.addInput(tout);
					tin.addInput(feedbackLevel);
				}	
			}
			//set up the delay
			final Delay d = new Delay(ac);
			ac.out.addInput(d);
			//set up a clock
			final Clock c = new Clock(ac, 200f);
			ac.out.addDependent(c);
			//get the clock to trigger an action
			c.addMessageListener(new Bead() {
				public void messageReceived(Bead message) {
					if(c.isBeat()) {
						if(Math.random() < 0.5) {
							int pitch = Pitch.forceToScale((int)(Math.random() * 64 + 32), Pitch.major);
							WavePlayer wp = new WavePlayer(ac, Pitch.mtof(pitch), Buffer.SQUARE);
							Envelope e = new Envelope(ac, 0f);
							Gain g = new Gain(ac, 1, e);
							g.addInput(wp);
							e.addSegment(0.1f, 500f);
							e.addSegment(0f, 40f, new KillTrigger(g));
							d.addInput(g);
						}
					}
				}
			});
			//go
			ac.start();
		}
	}
}
