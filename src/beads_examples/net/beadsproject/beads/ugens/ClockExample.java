package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.events.Pattern;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Static;


public class ClockExample {

	public static void main(String[] args) {
		System.out.println("Testing: " + Clock.class);
		AudioContext ac = new AudioContext(512);
		Envelope bpmEnv = new Envelope(ac, 120f);
		bpmEnv.addSegment(120f, 4000f);
		bpmEnv.addSegment(400f, 4000f);
		Function intervalEnv = new Function(bpmEnv) {
			public float calculate() {
				return 60000f / x[0];
			}
		};
		Clock clock = new Clock(ac, intervalEnv);
		ac.out.addDependent(clock);
		clock.setClick(true);
//		Pattern p = new Pattern();
//		p.addListener(new ClockListener());
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
