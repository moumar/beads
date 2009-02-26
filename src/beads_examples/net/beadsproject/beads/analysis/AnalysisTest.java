package net.beadsproject.beads.analysis;

import java.io.FileOutputStream;

import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.GnuplotDataWriter;
import net.beadsproject.beads.analysis.featureextractors.MFCC;
import net.beadsproject.beads.analysis.featureextractors.MelSpectrum;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;

public class AnalysisTest {

	public static void main(String[] args) throws Exception {
		AudioContext ac = new AudioContext();
		
		Envelope frequencyEnvelope1 = new Envelope(ac, 200f);
		frequencyEnvelope1.addSegment(20000, 1000f);
		WavePlayer wp1 = new WavePlayer(ac, frequencyEnvelope1, new SineBuffer().getDefault());
		ac.out.addInput(wp1);
		
		Envelope frequencyEnvelope2 = new Envelope(ac, 400f);
		frequencyEnvelope2.addSegment(20000, 500f);
		frequencyEnvelope2.addSegment(5000, 500f);
		WavePlayer wp2 = new WavePlayer(ac, frequencyEnvelope2, new SineBuffer().getDefault());
		ac.out.addInput(wp2);
		
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		sfs.addInput(wp1);
		sfs.addInput(wp2);
		ac.out.addDependent(sfs);
		
		sfs.setChunkSize(1024);
		sfs.setHopSize(256);

		sfs.addListener(new GnuplotDataWriter(new FileOutputStream("/Users/ollie/Desktop/sfs")));
	
		FFT fft = new PowerSpectrum();
		sfs.addListener(fft);

		fft.addListener(new GnuplotDataWriter(new FileOutputStream("/Users/ollie/Desktop/powerspec")));
		
		MelSpectrum ms = new MelSpectrum(ac.getSampleRate(), 128);
		fft.addListener(ms);

		ms.addListener(new GnuplotDataWriter(new FileOutputStream("/Users/ollie/Desktop/melspec")));
		
		MFCC mfcc = new MFCC(30);
		ms.addListener(mfcc);
		
		mfcc.addListener(new GnuplotDataWriter(new FileOutputStream("/Users/ollie/Desktop/mfcc")));
		
		DelayTrigger dt = new DelayTrigger(ac, 1000f, new AudioContextStopTrigger(ac));
		ac.out.addDependent(dt);

		ac.runNonRealTime();
	}
}
