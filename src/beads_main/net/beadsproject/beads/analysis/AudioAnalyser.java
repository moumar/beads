package net.beadsproject.beads.analysis;

import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

public class AudioAnalyser extends UGen {
	
	ShortFrameSegmenter segmenter;
	PeakDetector pd;
	
	public AudioAnalyser(AudioContext ac) {
		super(ac,1,0);		
		
		segmenter = new ShortFrameSegmenter(ac);
		segmenter.setChunkSize(2048);
		segmenter.setHopSize(441);
		setInputProxy(segmenter);
		
		outputInitializationRegime = OutputInitializationRegime.NULL;
		
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum();
		segmenter.addListener(fft);
		fft.addListener(ps);
		
		SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
		ps.addListener(sd);
		pd = new PeakDetector();
		sd.addListener(pd);
	}
	
	public PeakDetector peakDetector()
	{
		return pd;
	}

	@Override
	public void calculateBuffer(){}
}
