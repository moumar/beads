package net.beadsproject.beads.analysis;

import net.beadsproject.beads.analysis.featureextractors.Frequency;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.SamplePlayer;


public class SelfModifyingSampleExample {

	public static void main(String[] args) {
		
		//set up audio context and start
		AudioContext ac = new AudioContext();
		ac.start();
		
		//create sample player and plug into output
		final SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
		ac.out.addInput(sp);
		
		//create analysis components
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
		PowerSpectrum ps = new PowerSpectrum();
		final Frequency f = new Frequency(ac.getSampleRate()) {
			public void process(float[] f) {
				super.process(f);
				System.out.println(getFeatures()[0]);
			}
		};
		
		//connect analysis components together
		sfs.addListener(ps);
		ps.addListener(f);
		
		//plug in analysis (listens to SamplePlayer)
		ac.out.addDependent(sfs);
		sfs.addInput(sp);
		
		//now use the analysis to control the playback
		Clock x = new Clock(ac, 15000f);
//		SimplePowerOnsetDetector x = new SimplePowerOnsetDetector(ac);
		x.addInput(sp);
		ac.out.addDependent(x);
		x.addMessageListener(new Bead() {
			public void messageReceived(Bead b) {
				float features = f.getFeatures()[0];
				if(Float.isNaN(features)) features = 10000f;
				sp.getRateEnvelope().setValue(10000f / f.getFeatures()[0]);
			}
		});
	}
}
