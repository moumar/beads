package net.beadsproject.beads.core;

import java.io.File;
import java.io.IOException;

import org.jaudiolibs.beads.AudioServerIO;

import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.RecordToFile;
import net.beadsproject.beads.ugens.Reverb;

public class UsingAudioContext {
	
	public static class MostBasic {
		public static void main(String[] args) {
			/*
			 * Audio context has many constructors to allow you to set 
			 * the buffer size, IO and AudioFormat.
			 * This gives you the default AudioContext, using JavaSound.
			 */
			AudioContext ac = new AudioContext();
			//when you've got the hang of Beads check out some of the complexity of creating different AudioIOs
//			AudioContext ac = new AudioContext(128, AudioServerIO.createJackIO(), new IOAudioFormat(44100, 16, 2, 2));
			//make some sound
			Noise n = new Noise(ac);
			Gain g = new Gain(ac, 1, 0.05f);
			g.addInput(n);
			ac.out.addInput(g);
			//the AudioContext must be started
			ac.start();
		}
	}
	
	public static class NonRealTime {
		public static void main(String[] args) {
			AudioContext ac;
			DelayTrigger dt;
			/*
			 * In non-realitime mode the system just runs in an infinte loop from the
			 * thread you call it in. An AudioContextStopTrigger can be used to break the
			 * loop. In this case we trigger one from a DelayTrigger.
			 * 
			 * There are two ways to run in non-realtime.
			 * 
			 * (1) using any AudioIO, just start ac using runNonRealtime() instead of start()
			 */
			System.out.println("--- METHOD 1 ---");
			ac = new AudioContext();
			ac.logTime(true);
			dt = new DelayTrigger(ac, 10000, new AudioContextStopTrigger(ac));
			ac.out.addDependent(dt);
			ac.runNonRealTime();
			System.out.println("Method 1 Terminated");
			/*
			 * (2) use the NonRealtimeIO and just call start()
			 */
			System.out.println("--- METHOD 2 ---");
			ac = new AudioContext(new NonrealtimeIO());
			ac.logTime(true);
			dt = new DelayTrigger(ac, 10000, new AudioContextStopTrigger(ac));
			ac.out.addDependent(dt);
			ac.start();
			System.out.println("Method 2 Terminated");
		}
	}
	
	public static class AudioInput {
		public static void main(String[] args) {
			//using the default AudioIO - this may require you to set up an aggregate device on Mac OS X
			AudioContext ac = new AudioContext(128);
			//using jack audio IO (requires configuring jack)
//			AudioContext ac = new AudioContext(128, AudioServerIO.createJackIO(), new IOAudioFormat(44100, 16, 2, 2));
			ac.out.setGain(1f);
			/*
			 * Now get an audio input.
			 * 
			 * The array specifies which input channels you want access to.
			 */
			UGen input = ac.getAudioInput(new int[] {1});
			/*
			 * Let's just do something simple to the input so that you can hear it.
			 * In this case, put it through a Reverb.
			 */
			Reverb rb = new Reverb(ac);
			rb.addInput(input);
//			ac.out.addInput(rb);	//choose to hear through reverb
			ac.out.addInput(input);
			//go
			ac.start();
		}
	}
	
	public static class AudioInputRecord {
		public static void main(String[] args) throws IOException {
			//using the default AudioIO
			final AudioContext ac = new AudioContext();
			//a little recorder tool which streams straight to a file.
			//this must be killed in order for the raw audio file to get wav header.
			final RecordToFile rtf1 = new RecordToFile(ac, 1, new File("audio/temp.wav"));
			//the audio input. The array specified which input channels you want.
			UGen input = ac.getAudioInput(new int[] {1});
			rtf1.addInput(input);
			ac.out.addDependent(rtf1);
			DelayTrigger dt = new DelayTrigger(ac, 5000f, new Bead() {
				public void messageReceived(Bead message) {
					System.out.println("kill");
					rtf1.kill();
					ac.stop();
					System.exit(1);
				}
			});
			ac.out.addDependent(dt);
			//add some sound
			Noise n = new Noise(ac);
			Gain g = new Gain(ac, 1, 0.1f);
			g.addInput(n);
			ac.out.addInput(g);
			//go
			ac.start();
		}
	}
	
}
