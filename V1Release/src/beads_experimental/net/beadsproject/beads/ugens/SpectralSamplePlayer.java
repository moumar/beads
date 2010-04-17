package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;


public class SpectralSamplePlayer extends UGen {

	private Sample sample;

	private float[][] fftData;
	
	public SpectralSamplePlayer(AudioContext context, Sample sample) {
		super(context);
		this.sample = sample;
		//get the fftData from the sample
	}

	@Override
	public void calculateBuffer() {
		//grab the correct frame of FFT data and generate the waveform (with windowing)
	}

}
