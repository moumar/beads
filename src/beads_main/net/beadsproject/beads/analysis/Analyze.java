package net.beadsproject.beads.analysis;

import java.util.Hashtable;
import java.util.List;
import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.Frequency;
import net.beadsproject.beads.analysis.featureextractors.MFCC;
import net.beadsproject.beads.analysis.featureextractors.MelSpectrum;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.featureextractors.SpectralPeaks;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.SamplePlayer;


public class Analyze {

	public static class AnalysisSettings {
		int hopSize;
		int chunkSize;
	}
	
	private static AnalysisSettings defaultSettings;
	static {
		defaultSettings = new AnalysisSettings();
		defaultSettings.hopSize = 1024;
		defaultSettings.chunkSize = 2048;
	}
	
	public static FeatureSet sample(Sample s, List<String> extractors) {
		return sample(s, extractors, defaultSettings);
	}
	
	public static FeatureSet sample(Sample s, List<String> extractors, AnalysisSettings settings) {
		FeatureSet results = new FeatureSet();
		FeatureTrack lowLevel = new FeatureTrack();
		FeatureTrack beats = new FeatureTrack();
		results.add("Low Level", lowLevel);
		results.add("Beats", beats);
		Hashtable<String, Object> extractorArrangement = new Hashtable<String, Object>();
		AudioContext ac = new AudioContext();
		extractorArrangement.put("Context", ac);
		//set up call chain
		SamplePlayer sp = new SamplePlayer(ac, s);
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		sfs.setChunkSize(settings.chunkSize);
		sfs.setHopSize(settings.hopSize);
		ac.out.addDependent(sfs);
		sfs.addInput(sp); // left channel
		sfs.addSegmentListener(lowLevel);
		extractorArrangement.put("Segmenter", sfs);
		if(extractors != null) {
			for(String extractor : extractors) {
				if(extractor.equals("Power Spectrum")) {
					powerSpectrum(extractorArrangement);
				} else if(extractor.equals("FFT")) {
					fft(extractorArrangement);
				} else if(extractor.equals("Frequency")) {
					frequency(extractorArrangement);
				} else if(extractor.equals("MelSpectrum")) {
					melSpectrum(extractorArrangement);
				} else if(extractor.equals("MFCC")) {
					mfcc(extractorArrangement);
				} else if(extractor.equals("SpectralPeaks")) {
					spectralPeaks(extractorArrangement);
				}
			}
		}
		//inisit on spectral diff
		spectralDifference(extractorArrangement);
		//add low level stuff
		for(String featureName : extractorArrangement.keySet()) {
			if(extractorArrangement.get(featureName) instanceof FeatureExtractor<?, ?>) {
				lowLevel.addFeatureExtractor((FeatureExtractor<?, ?>)extractorArrangement.get(featureName));
			}
		}
		//add beat stuff
		PeakDetector d = new PeakDetector();
		SpectralDifference sd = (SpectralDifference)extractorArrangement.get("SpectralDifference");
		sd.addListener(d);
		d.addSegmentListener(beats);
		sp.setKillListener(new AudioContextStopTrigger(ac));
		ac.runNonRealTime();
		return results;
	}

	
	private static void spectralPeaks(Hashtable<String, Object> extractorArrangement) {
		if(!extractorArrangement.containsKey("SpectralPeaks")) {
			powerSpectrum(extractorArrangement);
			AudioContext ac = (AudioContext)extractorArrangement.get("Context");
			SpectralPeaks sp = new SpectralPeaks(ac, 200);
			PowerSpectrum ps = (PowerSpectrum)extractorArrangement.get("PowerSpectrum");
			ps.addListener(sp);
			extractorArrangement.put("SpectralPeaks", sp);
		}
	}
	
	private static void spectralDifference(Hashtable<String, Object> extractorArrangement) {
		if(!extractorArrangement.containsKey("SpectralDifference")) {
			powerSpectrum(extractorArrangement);
			AudioContext ac = (AudioContext)extractorArrangement.get("Context");
			SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
			PowerSpectrum ps = (PowerSpectrum)extractorArrangement.get("PowerSpectrum");
			ps.addListener(sd);
			extractorArrangement.put("SpectralDifference", sd);
		}
	}
	
	private static void mfcc(Hashtable<String, Object> extractorArrangement) {
		if(!extractorArrangement.containsKey("MFCC")) {
			melSpectrum(extractorArrangement);
			MFCC mfcc = new MFCC(20);
			MelSpectrum ms = (MelSpectrum)extractorArrangement.get("MelSpectrum");
			ms.addListener(mfcc);
			extractorArrangement.put("MFCC", mfcc);
		}
	}
	
	private static void melSpectrum(Hashtable<String, Object> extractorArrangement) {
		if(!extractorArrangement.containsKey("MelSpectrum")) {
			powerSpectrum(extractorArrangement);
			AudioContext ac = (AudioContext)extractorArrangement.get("Context");
			MelSpectrum ms = new MelSpectrum(ac.getSampleRate(), 100);
			PowerSpectrum ps = (PowerSpectrum)extractorArrangement.get("PowerSpectrum");
			ps.addListener(ms);
			extractorArrangement.put("MelSpectrum", ms);
		}
	}
	
	private static void frequency(Hashtable<String, Object> extractorArrangement) {
		if(!extractorArrangement.containsKey("Frequency")) {
			powerSpectrum(extractorArrangement);
			AudioContext ac = (AudioContext)extractorArrangement.get("Context");
			Frequency f = new Frequency(ac.getSampleRate());
			PowerSpectrum ps = (PowerSpectrum)extractorArrangement.get("PowerSpectrum");
			ps.addListener(f);
			extractorArrangement.put("Frequency", f);
		}
	}
	
	private static void powerSpectrum(Hashtable<String, Object> extractorArrangement) {
		if(!extractorArrangement.containsKey("PowerSpectrum")) {
			fft(extractorArrangement);
			PowerSpectrum ps = new PowerSpectrum();
			FFT fft = (FFT)extractorArrangement.get("FFT");
			fft.addListener(ps);
			extractorArrangement.put("PowerSpectrum", ps);
		}
	}
	
	private static void fft(Hashtable<String, Object> extractorArrangement) {
		if(!extractorArrangement.containsKey("FFT")) {
			FFT fft = new FFT();
			AudioSegmenter as = (AudioSegmenter)extractorArrangement.get("Segmenter");
			as.addListener(fft);
			extractorArrangement.put("FFT", fft);
		}
	}
	
}
