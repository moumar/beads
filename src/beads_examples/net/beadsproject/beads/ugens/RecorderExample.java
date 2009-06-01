package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.buffers.SawBuffer;

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
		AudioContext ac = new AudioContext();
		
		// Create an empty sample and recorder.
		float seconds = 0.1f;
		Sample s = new Sample(ac.getAudioFormat(),(long) (ac.getAudioFormat().getSampleRate()*seconds));
		s.clear();
		Recorder r = new Recorder(ac,s, Recorder.Mode.INFINITE);
		
		// set up something to record
		Envelope e = new Envelope(ac,440f);
		e.addSegment(220f, 50f, 0.01f, null);
		e.addSegment(110f, 50f);
		
		WavePlayer wp = new WavePlayer(ac,e,new SawBuffer().getDefault());
		r.addInput(wp);
		
		// play the sample		
		SamplePlayer sp = new SamplePlayer(ac,s);
		sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		sp.setKillOnEnd(false);
		Envelope rate = new Envelope(ac,2.f);
		rate.addSegment(5f, 5000f);
		rate.addSegment(0.1f, 5000f);
		sp.setRateEnvelope(rate);
		
		// attach the recorder and sample player
		ac.out.addDependent(r);
		ac.out.addInput(sp);		
		ac.start();
	}

}
