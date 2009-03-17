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

public class OnsetTestInteractive {

	public static void main(String[] args) throws Exception {
		AudioContext ac = new AudioContext();	
		
		//sample
		final SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
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
		SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
		//sd.setFreqWindow(80.f,1100.f);
		ps.addListener(sd);
		OnsetDetector od = new OnsetDetector();
		sd.addListener(od);
		od.setThreshold(0.2f);
		od.setAlpha(.9f);
		od.addMessageListener(new Bead(){protected void messageReceived(Bead b){System.out.print(".");}});
				
		//run
		ac.start();
	}
}
