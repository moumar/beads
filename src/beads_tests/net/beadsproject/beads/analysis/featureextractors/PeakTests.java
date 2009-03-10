package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileOutputStream;

import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.OneWindow;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;

public class PeakTests {

	public static void main(String[] args) throws Exception {
		AudioContext ac = new AudioContext();	
		//sample
		SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		ac.out.addInput(sp);
		//set up the chopper upper
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		sfs.addInput(ac.out);
		ac.out.addDependent(sfs);
		int chunkSize = 512;
		sfs.setChunkSize(chunkSize);
		sfs.setHopSize(chunkSize/2);
		
		//set up the fft
		FFT fft = new FFT();
		sfs.addListener(fft);
		PowerSpectrum ps = new PowerSpectrum();
		fft.addListener(ps);
				
		//set up spectral difference
		SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
		sd.setFreqWindow(80.f,1100.f);
		//sd.setFreqWindow(2000.f,10000.f);
		ps.addListener(sd);
		
		// Peak Detector		
		PeakDetector pd = new PeakDetector();
		sd.addListener(pd);		
		
		//print some data
		String analysisDataOutputDir = "output/peaks";		
		ps.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/ps")));
		sd.addListener(new BasicTimeDataWriter(ac, new FileOutputStream(analysisDataOutputDir + "/sd")));
		pd.addListener(new BasicTimeDataWriter(ac, new FileOutputStream(analysisDataOutputDir + "/pd")));
				
		// also output the waveform for comparison
		ShortFrameSegmenter basicseg = new ShortFrameSegmenter(ac);
		basicseg.setWindow(new OneWindow().getDefault());
		basicseg.setChunkSize(512);
		basicseg.setHopSize(512);
		basicseg.addInput(ac.out);
		ac.out.addDependent(basicseg);
		basicseg.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/wave")));
				
		//time the playback to 2s
		ac.runForNSecondsNonRealTime(2);
	}
}
