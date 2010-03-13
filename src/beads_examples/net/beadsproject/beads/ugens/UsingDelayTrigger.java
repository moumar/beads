package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;

public class UsingDelayTrigger {

	public static class Basic {
		public static void main(String[] args) {
			AudioContext ac = new AudioContext();
			DelayTrigger dt = new DelayTrigger(ac, 5000, new Bead() {
				public void messageReceived(Bead message) {
					System.out.println("message received");
				}
			});
			ac.out.addDependent(dt);
			ac.start();
		}
	}
}
