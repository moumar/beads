package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.Sample.TotalRegime;
import net.beadsproject.beads.ugens.core.Gain;
import net.beadsproject.beads.ugens.sample.SamplePlayer;

public class HowManySamplePlayers {

	//Try and thrash your system
	public final static int NUM_OSCS = 110;
	public final static int BUFFER_SIZE = 512;
	public final static int IO_BUFFER_SIZE = 1024;
	
	public static void main(String[] args) {
		final AudioContext ac = new AudioContext();
//		final AudioContext ac = new AudioContext(BUFFER_SIZE, IO_BUFFER_SIZE * 4);
//		ac.chooseMixerCommandLine();
//		Sample.Regime tr = Sample.Regime.newTotalRegimeNative();
//		SampleManager.setBufferingRegime(tr);
		
		Sample s = SampleManager.sample("audio/1234.aif");
		final Gain g = new Gain(ac, 2, 1f / NUM_OSCS);
		for(int i = 0; i < NUM_OSCS; i++) {
			SamplePlayer wp = new SamplePlayer(ac, s);
			// wp.setEnvelopeType(SamplePlayer.EnvelopeType.COARSE);
			wp.setLoopType(SamplePlayer.LoopType.NO_LOOP_FORWARDS);
			wp.setKillOnEnd(false);
			wp.getRateEnvelope().setValue((float)Math.random() + 0.5f);
			g.addInput(wp);
		}
		ac.out.addInput(g);
//		ac.out.addInput(new RTInput(ac));
//		ac.checkForDroppedFrames(false);
		ac.start();
	}
}
