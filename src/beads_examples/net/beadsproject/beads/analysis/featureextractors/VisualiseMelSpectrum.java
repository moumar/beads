package net.beadsproject.beads.analysis.featureextractors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.beadsproject.beads.analysis.SegmentListener;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Gain;

public class VisualiseMelSpectrum {

	@SuppressWarnings("serial")
	public static void main(String[] args) {

		//set up audio
		AudioContext ac = new AudioContext();

		//set up analysis
		final int NUM_FEATURES = 20;
		//set up segmenter
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		//set up power spectrum
		FFT fft = new FFT();
		PowerSpectrum ps = new PowerSpectrum();
		fft.addListener(ps);
		//attach power spectrum to segmenter
		sfs.addListener(fft);
		//set up melspectrum
		final MelSpectrum ms = new MelSpectrum(ac.getSampleRate(), NUM_FEATURES);
		ps.addListener(ms);
		
		//connect audio
		UGen in = ac.getAudioInput(new int[] {0});
		sfs.addInput(in);
		ac.out.addDependent(sfs);	//<-- sfs must be triggered
		//listen to input
		Gain g = new Gain(ac, 2, 0.5f);
		g.addInput(in);
		ac.out.addInput(g);
		
		//now make a drawing
		final JFrame f = new JFrame();
		JPanel p = new JPanel() {
			public void paintComponent(Graphics g) {
				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
				float[] spec = ms.getFeatures();
				float scale = 0.01f;
				if(spec != null) {
					g.setColor(Color.red);
					int boxWidth = getWidth() / spec.length;
					for(int i = 0; i < spec.length; i++) {
						int boxHeight = (int)(spec[i] * getHeight() * scale);
						g.fillRect(i * boxWidth, 
									getHeight() - boxHeight, 
									boxWidth, 
									boxHeight);
					}
				}
			}
		};
		f.add(p);
		f.setSize(new Dimension(500,500));
		f.setVisible(true);
		
		//make sure the drawing gets updated at each segment
		sfs.addSegmentListener(new SegmentListener() {
			public void newSegment(TimeStamp start, TimeStamp end) {
				f.repaint();
			}
		});
		
		//and go
		ac.start();
		
	}
}
