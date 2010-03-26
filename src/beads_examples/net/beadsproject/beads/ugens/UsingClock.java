package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;

public class UsingClock {

	public static class Basic {
		public static void main(String[] args) {

			// Create our audio context.
			AudioContext ac = new AudioContext();

			// Create a Clock object with a 1-second beat time (1000 ms).
			Clock clock = new Clock(ac, 1000);

			// Set the Clock to send a message only once per beat.
			clock.setTicksPerBeat(1);

			// Add the clock as a dependent. This ensures that it gets updated;
			// otherwise, it won't know that time is passing and won't do
			// anything.
			ac.out.addDependent(clock);

			// Create a new Bead that will output to the console when it
			// receives messages.
			Bead ticker = new Bead() {
				public void messageReceived(Bead message) {
					System.out.println("tick!");
				}
			};

			// Tell the clock to send messages to the ticker Bead.
			clock.addMessageListener(ticker);

			// Start processing audio.
			ac.start();

		}
	}
}
