package net.beadsproject.beads.analysis.featureextractors;

import java.util.Random;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralCentroid;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.OscillatorBank;
import net.beadsproject.beads.ugens.Static;
import net.beadsproject.beads.ugens.WavePlayer;


public class SpectralCentroidExample {

	public static void main(String[] args) {
		int NUM_OSCILLATORS = 20;
		Random rng = new Random();
		System.out.println("Testing: " + SpectralCentroid.class);
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
		//set up power spectrum
		PowerSpectrum ps = new PowerSpectrum();
		//attach power spectrum to segmenter
		sfs.addListener(ps);
		//set up spectral centroid
		final SpectralCentroid sc = new SpectralCentroid(ac.getSampleRate()) {
			public void process(float[] f) {
				super.process(f);
				float[] fts = getFeatures();
				for(int i = 0; i < fts.length; i++) {
					System.out.print(fts[i] + " ");
				}
				System.out.println();
			}
		};
		ps.addListener(sc);

		//bonus mark - make a sine wave play the spectral centroid
		WavePlayer wp = new WavePlayer(ac, 500f, new SineBuffer().getDefault()) {
			public void calculateBuffer() {
				getFrequencyEnvelope().setValue(sc.getFeatures()[0]);
				super.calculateBuffer();
			}
		};
		Gain wpGain = new Gain(ac, 1, new Static(ac, 0.1f));
		wpGain.addInput(wp);
		
		//connect audio
		sfs.addInput(g);
		ac.out.addDependent(sfs);	//<-- sfs must be triggered
		ac.out.addInput(g);
		ac.out.addInput(wpGain);
		ac.start();
	}

}
