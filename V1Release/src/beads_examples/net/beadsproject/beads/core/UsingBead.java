package net.beadsproject.beads.core;

import net.beadsproject.beads.ugens.Clock;

public class UsingBead {

	public static class OnTheFlyMessageReceiver {
		public static void main(String[] args) {
			/*
			 * Set up a clock, as an example of something that
			 * sends a message to a Bead.
			 */
			AudioContext ac = new AudioContext();
			Clock c = new Clock(ac);
			ac.out.addDependent(c);
			//now set up a Bead, on the fly
			Bead b = new Bead() {
				//and give it a messageReceived method
				public void messageReceived(Bead message) {
					/*
					 * Most things that send messages to a Bead
					 * are Beads themselves and send themselves
					 * as the argument. Since we're using a Clock
					 * to send the message we know that the arg
					 * will be a Clock.
					 */
					Clock c = (Clock)message;
					if(c.isBeat()) {
						if(c.getBeatCount() < 4) {
							System.out.println("Dying");
						} else {
							/*
							 * The kill command causes a Bead
							 * to be marked as dead. Many things
							 * such as Clock, and UGens, know to 
							 * look out for dead Beads and get rid
							 * of them.
							 */
							kill();
							System.out.println("Dead");
						}
					}
				}
			};
			/*
			 * Clocks send messages to message listeners.
			 * Make the new Bead listen to this Clock.
			 */
			c.addMessageListener(b);
			ac.start();
		}
	}
}
