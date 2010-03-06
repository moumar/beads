package net.beadsproject.beads.analysis.featureextractors;

import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.OscillatorBank;
import net.beadsproject.beads.ugens.Static;
import net.beadsproject.beads.ugens.WavePlayer;


public class SpectralPeaksExample {

	public static void main(String[] args) {
		final int NUM_OSCILLATORS = 10;
		final int NUM_PEAKS = 2;
		final float BASE_FREQ = 100.f;
		
		System.out.println("Testing: " + SpectralPeaks.class);
		//set up audio
		AudioContext ac = new AudioContext();
		//set up sound to analyse
		OscillatorBank ob = new OscillatorBank(ac, new SineBuffer().getDefault(), NUM_OSCILLATORS);
		float[] freqs = new float[NUM_OSCILLATORS];
		for(int i = 0; i < NUM_OSCILLATORS; i++) {
			freqs[i] = BASE_FREQ + i*100f;
		}
		ob.setFrequencies(freqs);
		Gain g = new Gain(ac, 1);
		//WavePlayer lfo = new WavePlayer(ac, 0.1f, new SineBuffer().getDefault());
		//g.setGainEnvelope(lfo);
		g.addInput(ob);

		//set up segmenter
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		final int CHUNK_SIZE = 2048*2;
		sfs.setChunkSize(CHUNK_SIZE);
		
		//set up power spectrum
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum();
		fft.addListener(ps);
		//attach power spectrum to segmenter
		sfs.addListener(fft);
		//set up spectral centroid
		final SpectralPeaks sp = new SpectralPeaks(ac, NUM_PEAKS) {
			public void process(TimeStamp a, TimeStamp b, float[] f) {
				super.process(a,b,f);
				double totalpower = 0;
				for (int i=0;i<NUM_PEAKS;i++)
				{
					double p = Math.sqrt(getFeatures()[i][1]/CHUNK_SIZE);
					totalpower += p;
					//System.out.printf("%.0f ",getFeatures()[i][0]);					
				}
				//System.out.printf("[%.2f]\n", totalpower);
			}
		};
		ps.addListener(sp);

		//bonus mark - make a sine wave play the spectral peak		
		WavePlayer wp = new WavePlayer(ac, 500f, new SineBuffer().getDefault()) {
			public void calculateBuffer() {
				getFrequencyEnvelope().setValue(sp.getFeatures()[0][0]);
				super.calculateBuffer();
			}
		};
		Gain wpGain = new Gain(ac, 1, new Static(ac, 0.1f));
		wpGain.addInput(wp);
		ac.out.addInput(wpGain);
		
		
		//connect audio
		sfs.addInput(g);
		ac.out.addDependent(sfs);	//<-- sfs must be triggered
		ac.out.addInput(g);
		ac.start();
	}

}
