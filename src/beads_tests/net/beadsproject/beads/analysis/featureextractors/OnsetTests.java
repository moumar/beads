package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileOutputStream;
import java.io.PrintStream;

import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.MeanFilter;
import net.beadsproject.beads.data.buffers.OneWindow;
import net.beadsproject.beads.data.buffers.RampBuffer;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.data.buffers.TriangularBuffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;

public class OnsetTests {

	public static void main(String[] args) throws Exception {
		AudioContext ac = new AudioContext();	
		//sample
		SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		// SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/01 Get Up.aif"));
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
		//sd.setFreqWindow(80.f,1100.f);
		ps.addListener(sd);
		
		String analysisDataOutputDir = "output/onsettests";
		OnsetDetector od = new OnsetDetector();
		
		// od.setFilter(new TriangularBuffer().generateBuffer(od.getBufferSize()));
		od.setThreshold(0.2f);
		od.setAlpha(.9f);
		// od.setFilter(new RampBuffer().generateBuffer(od.getBufferSize()));
		
		od.addThresholdListener(new BasicTimeDataWriter(ac, new FileOutputStream(analysisDataOutputDir + "/th")));
		sd.addListener(od);
		
		class OnsetTimePrinter extends Bead
		{			
			private AudioContext ac;			
			private PrintStream ps;
			
			public OnsetTimePrinter(AudioContext ac, FileOutputStream fos)
			{
				this.ac = ac;
				this.ps = new PrintStream(fos);				
			}
			
			protected void messageReceived(Bead message)
			{
				ps.print(ac.getTime());
				ps.print(" ");	
				ps.print(((OnsetDetector)message).getLastOnsetValue());								
				ps.println();
			}
		};		
		
		OnsetTimePrinter otp = new OnsetTimePrinter(ac,new FileOutputStream(analysisDataOutputDir + "/od"));
		od.addMessageListener(otp);
		
		//print some data			
		ps.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/ps")));
		sd.addListener(new BasicTimeDataWriter(ac, new FileOutputStream(analysisDataOutputDir + "/sd")));		
				
		// also output the waveform for comparison
		ShortFrameSegmenter basicseg = new ShortFrameSegmenter(ac);
		basicseg.setWindow(new OneWindow().getDefault());
		basicseg.setChunkSize(512);
		basicseg.setHopSize(512);
		basicseg.addInput(ac.out);
		ac.out.addDependent(basicseg);
		basicseg.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/wave")));
				
		//time the playback to 2s
		ac.runForNSecondsNonRealTime(4f);
	}
}
