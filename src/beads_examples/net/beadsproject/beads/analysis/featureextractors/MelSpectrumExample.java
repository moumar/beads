package net.beadsproject.beads.analysis.featureextractors;

import java.util.Random;

import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.core.Gain;
import net.beadsproject.beads.ugens.synth.OscillatorBank;
import net.beadsproject.beads.ugens.synth.WavePlayer;

public class MelSpectrumExample {

	public static void main(String[] args) {
		int NUM_OSCILLATORS = 20;
		int NUM_FEATURES = 20;
		Random rng = new Random();
		System.out.println("Testing: " + MelSpectrum.class);
		//set up audio
		AudioContext ac = new AudioContext();
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
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum() {
			public void process(TimeStamp startTime, TimeStamp endTime, float[][] input) {
				super.process(startTime, endTime, input);
				System.out.print("PS: ");
				for(float f : getFeatures()) {
					System.out.print(f + " ");
				}
				System.out.println();
			}
		};
		fft.addListener(ps);
		//attach power spectrum to segmenter
		sfs.addListener(fft);
		//set up melspectrum
		MelSpectrum ms = new MelSpectrum(ac.getSampleRate(), NUM_FEATURES) {
			public void process(TimeStamp startTime, TimeStamp endTime, float[] input) {
				super.process(startTime, endTime, input);
				System.out.print("MS: ");
				for(float f : getFeatures()) {
					System.out.print(f + " ");
				}
				System.out.println();
			}
		};
		ps.addListener(ms);
	
		//connect audio
		sfs.addInput(g);
		ac.out.addDependent(sfs);	//<-- sfs must be triggered
		ac.out.addInput(g);
		ac.start();
	}
}
