package net.beadsproject.beads.analysis;

import java.io.File;
import java.io.FileOutputStream;
import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.GnuplotDataWriter;
import net.beadsproject.beads.analysis.featureextractors.OnsetDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;

public class BasicOnsetDetectionExample {

	public static void main(String[] args) throws Exception {
		
		String workingdir = "/Users/ollie/Desktop";
		
		final AudioContext ac = new AudioContext();
		//set up playback
		final Sample samp = SampleManager.sample("audio/1234.aif");
		SamplePlayer player = new SamplePlayer(ac, samp);
		Gain sampleTrack = new Gain(ac, samp.nChannels);
		sampleTrack.addInput(player);
		ac.out.addInput(sampleTrack);
		//set up extractor stuff
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum();
		SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
		OnsetDetector d = new OnsetDetector();
		//connect extractor stuff
		sfs.addListener(fft);
		fft.addListener(ps);
		ps.addListener(sd);
		sd.addListener(d);
		//connect audio to extractor
		ac.out.addDependent(sfs);
		sfs.addInput(ac.out);
		//record some data
		ps.addListener(new GnuplotDataWriter<float[]>(new FileOutputStream(new File(workingdir + "/psData"))));
		sd.addListener(new GnuplotDataWriter<Float>(new FileOutputStream(new File(workingdir + "/sdData"))));
		//make a FeatureTrack to record the spectral difference data
		FeatureTrack spectralData = new FeatureTrack();
		sfs.addSegmentListener(spectralData);
		spectralData.addFeatureExtractor(sd);
		//make another feature track to record the onset times
		FeatureTrack onsetData = new FeatureTrack();
		d.addSegmentListener(onsetData);
		//set experiment to fixed length
		DelayTrigger dt = new DelayTrigger(ac, 3000f, new AudioContextStopTrigger(ac));
		ac.out.addDependent(dt);
		//go
		ac.runNonRealTime();
		//try saving the FeatureTrack to a FeatureSet
		FeatureSet fs = new FeatureSet(new File(workingdir + "/featureSet"));
		fs.add("spectralData", spectralData);
		fs.add("onsetData", onsetData);
		fs.write();
		//now try reading it again
		FeatureSet newFs = new FeatureSet(new File(workingdir + "/featureSet"));
		for(FeatureFrame ff : newFs.get("spectralData")) {
			System.out.println(ff);
		}		
		System.out.println("---------");
		for(FeatureFrame ff : newFs.get("onsetData")) {
			System.out.println(ff);
		}
	}
}
