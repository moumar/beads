package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;

public class UsingDelayTrigger {

	public static class Basic {
		public static void main(String[] args) {

			// Create our audio context.
			AudioContext ac = new AudioContext();

			// Create a DelayTrigger that sends a message to a new Bead after 5
			// seconds. That new Bead will then output to the console.
			DelayTrigger dt = new DelayTrigger(ac, 5000, new Bead() {
				public void messageReceived(Bead message) {
					System.out.println("message received");
				}
			});

			// Important! Adding the DelayTrigger as a dependent will make sure
			// that it gets updated - otherwise it won't know that time is
			// passing and won't fire.
			ac.out.addDependent(dt);

			// Start processing audio.
			ac.start();
		}
	}
}
