package net.beadsproject.beads.analysis;

import java.io.FileOutputStream;

import net.beadsproject.beads.analysis.featureextractors.Frequency;
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
		
		String analysisDataDir = "/Users/ollie/Desktop";
		
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
		
		sfs.setChunkSize(512);
		sfs.setHopSize(256);

		sfs.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataDir + "/sfs")));
	
		PowerSpectrum ps = new PowerSpectrum();
		sfs.addListener(ps);

		ps.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataDir + "/powerspec")));
		
		MelSpectrum ms = new MelSpectrum(ac.getSampleRate(), 128);
		ps.addListener(ms);

		ms.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataDir + "/melspec")));
		
		MFCC mfcc = new MFCC(30);
		ms.addListener(mfcc);
		
		mfcc.addListener(new GnuplotDataWriter(new FileOutputStream(analysisDataDir + "/mfcc")));
		
		Frequency f = new Frequency(ac.getSampleRate()) {
			public void process(float[] data) {
				super.process(data);
				System.out.println(features[0]);
			}
		};
		ps.addListener(f);
		
		DelayTrigger dt = new DelayTrigger(ac, 1000f, new AudioContextStopTrigger(ac));
		ac.out.addDependent(dt);

		ac.runNonRealTime();
	}
}
