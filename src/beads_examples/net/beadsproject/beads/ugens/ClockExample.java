package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Static;


public class ClockExample {

	public static void main(String[] args) {
		System.out.println("Testing: " + Clock.class);
		AudioContext ac = new AudioContext(512);
		Clock clock = new Clock(ac, new Static(ac, 500f));
		ac.out.addDependent(clock);
		clock.setClick(true);
		clock.addMessageListener(new ClockListener());
		ac.start();
	}

	private static class ClockListener extends Bead {
		public void messageReceived(Bead message) {
			Clock clock = (Clock)message;
			System.out.println(clock.getCount());
		}
	}
}
