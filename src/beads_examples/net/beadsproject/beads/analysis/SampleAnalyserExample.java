package net.beadsproject.beads.analysis;

import net.beadsproject.beads.analysis.FeatureLayer;
import net.beadsproject.beads.analysis.SampleAnalyser;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralCentroid;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;

public class SampleAnalyserExample {

	public static void main(String[] args) {
		System.out.println("Testing: " + SampleAnalyser.class);

		//set up segmenter
		AudioContext ac = new AudioContext(64);
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		//set up power spectrum
		PowerSpectrum ps = new PowerSpectrum();
		//attach power spectrum to segmenter
		sfs.addResponderExtractor(ps);
		//set up spectral centroid
		final SpectralCentroid sc = new SpectralCentroid(ac.getSampleRate());
		ps.addListener(sc);
		sfs.addExtractor(sc);
		
		//add FeatureLayer to Segmenter (this turns on recording)
		sfs.setFeatureLayer(new FeatureLayer());
		
		//load a sample, do the analysis
		Sample s = SampleManager.sample("audio/1234.aif");
		SampleAnalyser sa = new SampleAnalyser(s);
		sa.analyseSample(sfs);
		sa.printLayers();
	}
	
}
