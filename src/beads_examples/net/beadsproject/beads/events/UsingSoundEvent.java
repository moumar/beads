package net.beadsproject.beads.events;

import java.util.Hashtable;
import java.util.Map;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Reverb;
import net.beadsproject.beads.ugens.WavePlayer;

public class UsingSoundEvent {

	public static void main(String[] args) {
		Basic.main(args);
	}
	
	public static class Basic {
		public static void main(String[] args) {
			/*
			 * A SoundEvent is a way of encapsulating a single sonic behaviour which
			 * can be triggered elsewhere. It could be useful to define a synth sound, 
			 * for example, which then gets played polyphonically. The principle behind
			 * a SoundEvent is that it is responsible for setting itself up and adding
			 * itself to the environment using the output and parameters arguments. 
			 * 
			 * The parameters argument is simply a Map<String, Object> so it is up to
			 * you to make sure that the calling class provides appropriate arguments
			 * here. For a synth, for example, you might pass some global objects such
			 * as an LFO.
			 * 
			 * A SoundEvent also returns the sound it has just created in play(). This is
			 * so that the calling class has the ability to kill sounds, for example, if
			 * polyphony has got too high. However, typically SoundEvent sounds take care
			 * of killing themselves.
			 * 
			 * Create a sound event by overriding the play() method. Within this
			 * method, use the output argument to send your sound at.
			 */
			final SoundEvent mySoundEvent = new SoundEvent() {
				/*
				 * This sound event plays a random note from a dorian scale
				 *  and uses the parameter "mod" to modulate the frequency of 
				 *  that note.
				 */
				public UGen play(UGen output, Map<String, Object> parameters) {
					//grab audio context from output object
					AudioContext ac = output.getContext();
					//generate a random freq
					final float freq = Pitch.mtof(
											Pitch.forceToScale((int)(Math.random() * 64 + 32), 
											Pitch.dorian));
					//generate a random mod level
					final float modLevel = (float)Math.random();
					//create on-the-fly UGen to apply mod to frequency
					Function freqMod = new Function((WavePlayer)parameters.get("mod")) {
						//see UsingFunction for more details on this
						@Override
						public float calculate() {
							return freq + x[0] * modLevel * 10000f;
						}
					};
					//WavePlayer that makes the sound, using freqMod for frequency
					WavePlayer wp = new WavePlayer(ac, freqMod, Buffer.SAW);
					//Envelope to control the Gain
					Envelope e = new Envelope(ac, 0.1f);
					//The Gain, uses Envelope e
					Gain g = new Gain(ac, 1, e);
					//Connect the WavePlayer to the Gain
					g.addInput(wp);
					//set e to fade to zero and then kill g
					e.addSegment(0, 200, new KillTrigger(g));
					//connect to output
					output.addInput(g);
					//return g so that caller can kill if required
					return g;
				}
			};
			//Now we have a SoundEvent, do something with it
			AudioContext ac = new AudioContext();
			//set up parameters with "mod", see above how this is used
			final Map<String, Object> params = new Hashtable<String, Object>();
			params.put("mod", new WavePlayer(ac, 100, Buffer.TRIANGLE));
			//we'll go through a Reverb on the way out
			final Reverb rb = new Reverb(ac);
			ac.out.addInput(rb);
			//use a Clock to trigger the sounds
			final Clock c = new Clock(ac, 200f);
			ac.out.addDependent(c);
			//custom Bead to respond to Clock ticks
			c.addMessageListener(new Bead() {
				public void messageReceived(Bead message) {
					if(c.isBeat()) {
						//play the sound each beat
						mySoundEvent.play(rb, params);
					}
				}
			});
			//Tally ho (don't forget me when you get signed to Warp)
			ac.start();
		}
	}
}
