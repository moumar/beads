package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import net.beadsproject.beads.analysis.featureextractors.SpectralDifference.DifferenceType;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;

/** 
 *  Tests the Spectral Difference Class
 * @author ben
 *
 */
public class SpectralDifferenceTestTwo {
	public static void main(String args[]) throws FileNotFoundException
	{
		AudioContext ac = new AudioContext();	
		Envelope fe = new Envelope(ac, 50.f);
		fe.addSegment(20000.f,1000.f);		
		
		final Sample samp = SampleManager.sample("audio/1234.aif");
		SamplePlayer player = new SamplePlayer(ac, samp);
		ac.out.addInput(player);
				
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum();		
		fft.addListener(ps);		
		
		// mean difference
		SpectralDifference sdmean = new SpectralDifference(ac.getSampleRate()); // ,10000.f,20000.f);
		sdmean.setDifferenceType(DifferenceType.MEANDIFFERENCE);
		
		// rms 
		SpectralDifference sdrms = new SpectralDifference(ac.getSampleRate()); // ,10000.f,20000.f);
		sdrms.setDifferenceType(DifferenceType.RMS);

		// high frequencies only (rms by default)
		SpectralDifference sdhigh = new SpectralDifference(ac.getSampleRate(),1500.f,10000.f);
		
		// vocal range frequencies (80Hz-1100Hz)
		SpectralDifference sdvoc = new SpectralDifference(ac.getSampleRate(),80.f,1100.f);
		
		ps.addListener(sdmean);
		ps.addListener(sdrms);
		ps.addListener(sdhigh);
		ps.addListener(sdvoc);
		
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		sfs.addInput(ac.out);
		ac.out.addDependent(sfs);
		int chunksize = 512;
		sfs.setChunkSize(chunksize);
		sfs.setHopSize(chunksize/2);

		sfs.addListener(fft);
		
		// output data
		String analysisDataOutputDir = "output/spectraldifferencetesttwo";
		sfs.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sfs")));
		ps.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/ps")));
		sdmean.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sdmean")));
		sdrms.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sdrms")));
		sdhigh.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sdhigh")));
		sdvoc.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sdvoc")));
		
		// run for 1 second		
		ac.runForNSecondsNonRealTime(3);
	}

}
