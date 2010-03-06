package net.beadsproject.beads.analysis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.core.Gain;
import net.beadsproject.beads.ugens.sample.SamplePlayer;
import net.beadsproject.beads.ugens.utility.DelayTrigger;

public class BeatChopperExample {
	
	public static void main(String[] args) throws Exception {
		final Sample samp = SampleManager.sample("audio/1234.aif");
		final FeatureTrack onsetData = analyse(samp, 0.5f, 0.5f);
		//take a peek at the data
		for(FeatureFrame ff : onsetData) {
			System.out.println(ff);
		}
		System.out.println("Total segments: " + onsetData.getNumberOfFrames());
		//now let's try to play back the segments
		final AudioContext ac = new AudioContext();
		ac.start();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		for(int i = 0; i < onsetData.getNumberOfFrames(); i++) {
			JButton b = new JButton("" + i);
			final int code = i;
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println(code);
					SamplePlayer s = new SamplePlayer(ac, samp);
					double start = onsetData.get(code).getStartTimeMS();
					double end = onsetData.get(code).getEndTimeMS();
					s.setPosition(start);
					DelayTrigger dt = new DelayTrigger(ac, end - start, new  KillTrigger(s));
					ac.out.addInput(s);
					ac.out.addDependent(dt);
				}
			});
			p.add(b);
		}
		JFrame f = new JFrame();
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
	}
	
	public static FeatureTrack analyse(Sample s, float alpha, float thresh) {
		//// (1) ////
		//set up audio context
		AudioContext ac = new AudioContext();
		//set up playback
		SamplePlayer player = new SamplePlayer(ac, s);
		ac.out.addInput(player);
		//set experiment to length of sample
		player.setKillListener(new AudioContextStopTrigger(ac));
		//// (2) ////
		//set up analysis classes
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum();
		SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
		PeakDetector d = new PeakDetector();
		//connect together extractor classes
		sfs.addListener(fft);
		fft.addListener(ps);
		ps.addListener(sd);
		sd.addListener(d);
		//connect audio to extractor
		ac.out.addDependent(sfs);
		sfs.addInput(ac.out);
		//make a feature track to record the onset times to
		FeatureTrack onsetData = new FeatureTrack();
		d.addSegmentListener(onsetData);
		onsetData.addFeatureExtractor(d);
		//// (3) ////
		//play with the onset detection settings
		d.setAlpha(alpha);
		d.setThreshold(thresh);
		//run
		ac.runNonRealTime();
		//return
		return onsetData;
	}
}
