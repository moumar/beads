package net.beadsproject.beads.core;

import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;

public class UsingAudioContext {

	public static class MostBasic {
		public static void main(String[] args) {
			/*
			 * Audio context has many constructors to allow you to set 
			 * the buffer size, IO and AudioFormat.
			 * This gives you the default AudioContext, using JavaSound.
			 */
			AudioContext ac = new AudioContext();
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
			//use the default AudioIO: JavaSoundAudioIO
			AudioContext ac = new AudioContext();
			ac.out.setGain(0.1f);
			/*
			 * Now get an audio input.
			 * 
			 * The array specified which input channels you want.
			 */
			UGen input = ac.getAudioInput(new int[] {0});
			ac.out.addInput(input);
			//go
			ac.start();
		}
	}
	
}
