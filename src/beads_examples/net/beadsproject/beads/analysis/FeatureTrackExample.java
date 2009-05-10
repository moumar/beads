package net.beadsproject.beads.analysis;

import java.util.Random;
import net.beadsproject.beads.analysis.FeatureFrame;
import net.beadsproject.beads.analysis.FeatureTrack;
import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.ReBin;
import net.beadsproject.beads.analysis.featureextractors.SpectralCentroid;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.OscillatorBank;
import net.beadsproject.beads.ugens.WavePlayer;

public class FeatureTrackExample {

	public static void main(String[] args) {
		int NUM_OSCILLATORS = 100;
		Random rng = new Random();
		System.out.println("Testing: " + FeatureTrack.class);
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
		//set up fft
		FFT fft = new FFT();
		//attach fft to segmenter
		sfs.addListener(fft);
		//set up power spectrum
		PowerSpectrum ps = new PowerSpectrum();
		//attach power spectrum to fft
		fft.addListener(ps);
		//set up spectral centroid
		final SpectralCentroid sc = new SpectralCentroid(ac.getSampleRate());
		ps.addListener(sc);
		sfs.addListener(sc);
		//set up rebinner
		ReBin rb = new ReBin(10);
		ps.addListener(rb);
		
		//set up FeatureTrack and FeatureRecorder
		FeatureTrack ft = new FeatureTrack() {
			public void add(FeatureFrame ff) {
				super.add(ff);
				System.out.println("received feature frame: ");
				System.out.println(ff);
			}
		};
		FeatureRecorder fr = new FeatureRecorder();
		sfs.addRecorder(fr);
		fr.addFeatureExtractor(ps);
		fr.addFeatureExtractor(sc);
		fr.addFeatureExtractor(rb);
		fr.setFeatureTrack(ft);
		
		//connect audio
		sfs.addInput(g);
		ac.out.addDependent(sfs);	//<-- sfs must be triggered
		ac.out.addInput(g);
		ac.start();
	}
}
