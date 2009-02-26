package net.beadsproject.beads.analysis.featureextractors;

import java.util.Random;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.OscillatorBank;
import net.beadsproject.beads.ugens.WavePlayer;

public class PowerSpectrumExample {
	
	public static void main(String[] args) {
		int NUM_OSCILLATORS = 20;
		Random rng = new Random();
		System.out.println("Testing: " + PowerSpectrum.class);
		//set up audio
		AudioContext ac = new AudioContext(512, 5000);
		//set up sound to analyse
		OscillatorBank ob = new OscillatorBank(ac, new SineBuffer().getDefault(), NUM_OSCILLATORS);
		float[] freqs = new float[NUM_OSCILLATORS];
		for(int i = 0; i < NUM_OSCILLATORS; i++) {
			freqs[i] = rng.nextFloat() * 1000f + 100f;
		}
		ob.setFrequencies(freqs);
		Gain g = new Gain(ac, 1);
		WavePlayer lfo = new WavePlayer(ac, 0.1f, new SineBuffer().getDefault());
		g.setGainEnvelope(lfo);
		g.addInput(ob);

		//set up segmenter
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		//set up power
		PowerSpectrum ps = new PowerSpectrum() {
			public void process(float[] f) {
				super.process(f);
				float[] fts = getFeatures();
				for(int i = 0; i < fts.length; i++) {
					System.out.print(fts[i] + " ");
				}
				System.out.println();
			}
		};
		//attach power to segmenter
		sfs.addListener(ps);

		//connect audio
		sfs.addInput(g);
		ac.out.addDependent(sfs);	//<-- sfs must be triggered
		ac.out.addInput(g);
		ac.start();
	}
}
