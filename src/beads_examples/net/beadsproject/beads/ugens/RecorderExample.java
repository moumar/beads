package net.beadsproject.beads.ugens;

import java.io.IOException;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.buffers.SawBuffer;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.core.Envelope;
import net.beadsproject.beads.ugens.synth.WavePlayer;
import net.beadsproject.beads.ugens.utility.DelayTrigger;
import net.beadsproject.beads.ugens.utility.Recorder;

/**
 * Demonstration of recording into a sample, and playing it back.
 * 
 * Supposed to sound nasty... :P
 * 
 * @author ben
 *
 */
public class RecorderExample {
	public static void main(String[] args) throws Exception {
		final AudioContext ac = new AudioContext();
		
		// Create an empty sample and recorder.
		float ms = 100f; 
		final Sample s = new Sample(ac.getAudioFormat(), ms);
		s.clear();
		final Recorder r = new Recorder(ac,s);
		
		
		// set up something to record
		Envelope e = new Envelope(ac,440f);
		e.addSegment(220f, 50f, 0.01f, null);
		e.addSegment(110f, 50f);
		
		WavePlayer wp = new WavePlayer(ac,e,new SawBuffer().getDefault());
		r.addInput(wp);
		
		
		
		// attach the recorder
		ac.out.addDependent(r);
		
		
		// option 1: play the sample whilst it's being filled
		/*
		SamplePlayer sp = new SamplePlayer(ac,s);
		sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		sp.setKillOnEnd(false);
		Envelope rate = new Envelope(ac,2.f);
		rate.addSegment(5f, 5000f);
		rate.addSegment(0.1f, 5000f);
		sp.setRateEnvelope(rate);
		ac.out.addInput(sp);		
		ac.start();
		*/
		
		// option 2: set a time limit and then save the sample
		
		DelayTrigger dt = new DelayTrigger(ac, 1000, new Bead() {
			public void messageReceived(Bead message) {
				ac.stop();
				System.out.println("stopped");
				try {					
					r.clip();
					s.write("audio/test.aif");
					System.out.println("saved");
					System.exit(1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		ac.out.addDependent(dt);
		ac.out.addInput(wp);
		ac.start();
		
		
		// option 3: save the sample continuously until a given time
	}

}
