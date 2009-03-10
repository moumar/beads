package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileOutputStream;
import net.beadsproject.beads.analysis.featureextractors.Frequency;
import net.beadsproject.beads.analysis.featureextractors.GnuplotDataWriter;
import net.beadsproject.beads.analysis.featureextractors.MFCC;
import net.beadsproject.beads.analysis.featureextractors.MelSpectrum;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;

public class FFTCompareTest {

	public static void main(String[] args) throws Exception {
		AudioContext ac = new AudioContext();	
		//various sounds, switch on and off
		//jumping wave
		Envelope frequencyEnvelope1 = new Envelope(ac, 50.f);
		// frequencyEnvelope1.addSegment(1000.f,1000.f);		
		WavePlayer wp1 = new WavePlayer(ac, frequencyEnvelope1, new SineBuffer().getDefault());
		ac.out.addInput(wp1);	
		
		//set up the chopper upper
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		sfs.addInput(ac.out);
		ac.out.addDependent(sfs);
		int chunksize = 1024;
		sfs.setChunkSize(chunksize);
		sfs.setHopSize(chunksize/2);
		
		FFT fft = new FFT();
		SlowFFTWrapper fft2 = new SlowFFTWrapper();	
		PowerSpectrum ps = new PowerSpectrum();
		SlowPowerSpectrum ps2 = new SlowPowerSpectrum();
		
		sfs.addListener(fft);
		sfs.addListener(fft2);
		fft.addListener(ps);		
		fft2.addListener(ps2);		
		
		Frequency freq = new Frequency(ac.getSampleRate())
		{
			public void process(float[] f)
			{
				super.process(f);
				System.out.println(features[0]);
			}
		};
		
		ps2.addListener(freq);
		
		//get the frequencies of the bins in the fft
		float binFreqs[] = new float[sfs.getChunkSize()];
		for(int i=0;i<binFreqs.length;i++)
		{
			System.out.print(i);
			System.out.print(" ");
			System.out.print(FFT.binFrequency(ac.getSampleRate(), binFreqs.length, i));
			System.out.print(" ");
		}
		
		//print some data
		String analysisDataOutputDir = "C:/tmp";	
		sfs.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sfs")));		
		ps.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/ps")));
		ps2.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/ps2")));
		
		//time the playback to 2s
		DelayTrigger dt = new DelayTrigger(ac, 1000f, new AudioContextStopTrigger(ac));
		ac.out.addDependent(dt);
		//run offline
		ac.runNonRealTime();
	}
}
