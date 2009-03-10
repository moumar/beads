package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import net.beadsproject.beads.analysis.featureextractors.SpectralDifference.DifferenceType;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;

/** 
 *  Tests the Spectral Difference Class
 * @author ben
 *
 */
public class SpectralDifferenceTest {
	public static void main(String args[]) throws FileNotFoundException
	{
		AudioContext ac = new AudioContext();	
		Envelope fe = new Envelope(ac, 50.f);
		fe.addSegment(20000.f,1000.f);		
		
		WavePlayer wpSin = new WavePlayer(ac, fe, new SineBuffer().getDefault());
		ac.out.addInput(wpSin);
				
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum();		
		fft.addListener(ps);		
		
		SpectralDifference sd = new SpectralDifference(ac.getSampleRate(),10000.f,20000.f);
		sd.setDifferenceType(DifferenceType.MEANDIFFERENCE);
		ps.addListener(sd);
		
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		sfs.addInput(ac.out);
		ac.out.addDependent(sfs);
		int chunksize = 512;
		sfs.setChunkSize(chunksize);
		sfs.setHopSize(chunksize/2);

		sfs.addListener(fft);
		
		// output data
		String analysisDataOutputDir = "C:/tmp";
		sfs.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sfs")));
		ps.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/ps")));
		sd.addListener(new BasicDataWriter(new FileOutputStream(analysisDataOutputDir + "/sd")));
		
		// run for 1 second		
		ac.runForNSecondsNonRealTime(1);
	}

}
