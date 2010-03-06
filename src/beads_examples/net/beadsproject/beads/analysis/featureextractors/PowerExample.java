package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.featureextractors.Power;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.core.Gain;
import net.beadsproject.beads.ugens.synth.WavePlayer;

public class PowerExample {
	
	public static void main(String[] args) {
		System.out.println("Testing: " + Power.class);
		//set up audio
		AudioContext ac = new AudioContext();
		//set up sound to analyse
		WavePlayer wp = new WavePlayer(ac, 500f, new SineBuffer().getDefault());
		Gain g = new Gain(ac, 1);
		WavePlayer lfo = new WavePlayer(ac, 0.1f, new SineBuffer().getDefault());
		g.setGainEnvelope(lfo);
		g.addInput(wp);

		//set up segmenter
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		//set up power
		Power p = new Power() {
			public void process(TimeStamp st, TimeStamp et, float[] f) {
				super.process(st, et, f);
				System.out.println(getFeatures());
			}
		};
		//attach power to segmenter
		sfs.addListener(p);

		//connect audio
		sfs.addInput(g);
		ac.out.addDependent(sfs);	//<-- sfs must be triggered
		ac.out.addInput(g);
		ac.start();
	}
	
}
