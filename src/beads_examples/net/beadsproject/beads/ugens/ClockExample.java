package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;

public class ClockExample {

	public static void main(String[] args) {
		System.out.println("Testing: " + Clock.class);
		//create an audio context
		AudioContext ac = new AudioContext();
		//create an envelope that controls BPM
		Envelope bpmEnv = new Envelope(ac, 120f);
		//do some stuff to the BPM
		bpmEnv.addSegment(120f, 4000f);
		bpmEnv.addSegment(400f, 4000f);
		//create a function that maps BPM to millisecond interval
		Function intervalEnv = new Function(bpmEnv) {
			public float calculate() {
				return 60000f / x[0];
			}
		};
		//create the clock with the interval envelope
		Clock clock = new Clock(ac, intervalEnv);
		//the clock has no outputs, so it needs to be added as a dependent
		//to the signal chain
		ac.out.addDependent(clock);
		//optional click, goes straight to ac.out
		clock.setClick(true);
		//see ClockListener class below
		clock.addMessageListener(new ClockListener());
		//start the system
		ac.start();
	}

	private static class ClockListener extends Bead {
		public void messageReceived(Bead message) {
			Clock clock = (Clock)message;
			System.out.println(clock.getCount());
		}
	}
}
