package net.beadsproject.beads.analysis;

import java.io.FileOutputStream;

import net.beadsproject.beads.analysis.featureextractors.BasicDataWriter;
import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.Frequency;
import net.beadsproject.beads.analysis.featureextractors.GnuplotDataWriter;
import net.beadsproject.beads.analysis.featureextractors.MFCC;
import net.beadsproject.beads.analysis.featureextractors.MelSpectrum;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;

public class AnalysisTest {

	public static void main(String[] args) throws Exception {
		AudioContext ac = new AudioContext();	
		//various sounds, switch on and off
		//jumping wave
		Envelope frequencyEnvelope1 = new Envelope(ac, Pitch.mtof(60));
//		frequencyEnvelope1.addSegment(200, 200f);
//		frequencyEnvelope1.addSegment(500, 10f);
//		frequencyEnvelope1.addSegment(500, 200f);
//		frequencyEnvelope1.addSegment(1000, 10f);
//		frequencyEnvelope1.addSegment(1000, 200f);
//		frequencyEnvelope1.addSegment(1500, 10f);
//		frequencyEnvelope1.addSegment(1500, 200f);
//		frequencyEnvelope1.addSegment(2000, 10f);
		WavePlayer wp1 = new WavePlayer(ac, frequencyEnvelope1, new SineBuffer().getDefault());
//		ac.out.addInput(wp1);	
//		//sliding wave
//		Envelope frequencyEnvelope2 = new Envelope(ac, 400f);
//		frequencyEnvelope2.addSegment(20000, 500f);
//		frequencyEnvelope2.addSegment(5000, 500f);
//		WavePlayer wp2 = new WavePlayer(ac, frequencyEnvelope2, new SineBuffer().getDefault());
//		ac.out.addInput(wp2);
		//sample
		SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		ac.out.addInput(sp);
		//set up the chopper upper
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		sfs.addInput(ac.out);
		ac.out.addDependent(sfs);
		int chunkSize = 1024;
		sfs.setChunkSize(chunkSize);
		sfs.setHopSize(chunkSize/2);
		//set up the fft
		FFT fft = new FFT();
		sfs.addListener(fft);
		//set up the power spectrum
		PowerSpectrum ps = new PowerSpectrum();
		fft.addListener(ps);
		//set up the mel spectrum filterbank
		MelSpectrum ms = new MelSpectrum(ac.getSampleRate(), 40);
		ps.addListener(ms);
		//set up the mfcc-ifyer
		MFCC mfcc = new MFCC(13);
		ms.addListener(mfcc);
		//set up frequency - just prints to System.out.
		Frequency f = new Frequency(ac.getSampleRate()) {
			public void process(float[] data) {
				super.process(data);
				System.out.println(Pitch.ftom(features[0]));
			}
		};
		ps.addListener(f);
		//set up spectral difference
		SpectralDifference sd = new SpectralDifference();
		sd.setMinBin(0);
		sd.setMaxBin(chunkSize/2);
		ps.addListener(sd);
		
		// Peak Detector
		PeakDetector pd = new PeakDetector();
		sd.addListener(pd);		
		
		//print some data
		String analysisDataOutputDir = "C:/tmp";	
		//sfs.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataOutputDir + "/sfs")));
//		fft.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataOutputDir + "/fft")));
		//ps.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataOutputDir + "/powerspec")));
		//ms.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataOutputDir + "/melspec")));
		//mfcc.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataOutputDir + "/mfcc")));
		sd.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sd")));
		pd.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/pd")));		
		
		//time the playback to 2s
		DelayTrigger dt = new DelayTrigger(ac, 4000f, new AudioContextStopTrigger(ac));
		ac.out.addDependent(dt);
		//run offline
		ac.runNonRealTime();
	}
}
