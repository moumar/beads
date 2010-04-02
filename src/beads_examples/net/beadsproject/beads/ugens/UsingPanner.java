package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;
import net.beadsproject.beads.data.Buffer;

public class UsingPanner {

	public static class Basic {
		public static void main(String[] args) {

			// A boring example: panning to the left. See the moderate example
			// for more fun.

			// Create our audio context.
			AudioContext ac = new AudioContext();

			// We'll use a nice, simple sine tone.
			WavePlayer sine = new WavePlayer(ac, 220, Buffer.SINE);

			// Create our Panner.
			Panner panner = new Panner(ac);

			// And we'll pan it mostly to the left.
			panner.setPos(-.5f);

			// Now add the sine tone to the Panner's input.
			panner.addInput(sine);

			// And send the output to the audio out.
			ac.out.addInput(panner);

			// Don't forget to start processing audio!
			ac.start();

		}
	}

	public static class Moderate {
		// We have to declare this out here...
		static Envelope envelope;

		public static void main(String[] args) {

			// Moving bloops.

			// Create our audio context.
			AudioContext ac = new AudioContext();

			// We'll use a nice, simple sine tone.
			WavePlayer sine = new WavePlayer(ac, 220, Buffer.SINE);

			// We'll make it bloop-y by multiplying it (using a Gain) by an
			// envelope that is triggered by a Clock.
			envelope = new Envelope(ac, 0);
			Gain bloops = new Gain(ac, 1, envelope);
			bloops.addInput(sine);

			Clock clock = new Clock(ac, 500);
			clock.setTicksPerBeat(1);
			ac.out.addDependent(clock);

			Bead blooper = new Bead() {
				public void messageReceived(Bead message) {
					envelope.addSegment(1, 30);
					envelope.addSegment(.1f, 30);
					envelope.addSegment(0, 60);
				}
			};

			clock.addMessageListener(blooper);

			// Now we'll make the pan position move back and forth with a slow
			// sine wave.
			WavePlayer panPos = new WavePlayer(ac, 1, Buffer.SINE);

			// Create our Panner.
			Panner panner = new Panner(ac);

			// Use our slow sine wave as the position.
			panner.setPos(panPos);

			// Now add the bloops to the Panner's input.
			panner.addInput(bloops);

			// And send the output to the audio out.
			ac.out.addInput(panner);

			// Don't forget to start processing audio!
			ac.start();

		}
	}

}
