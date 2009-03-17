package net.beadsproject.beads.analysis;

import java.io.IOException;
import net.beadsproject.beads.analysis.segmenters.SimplePowerOnsetDetector;
import net.beadsproject.beads.analysis.segmenters.SpectralDifferenceOnsetDetector;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clicker;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Recorder;
import net.beadsproject.beads.ugens.SamplePlayer;


public class OnsetDetectionTest {

	public static void main(String[] args) {
		
		String audioDir = "/Users/ollie/Programming/Eclipse/Onset Detector Tests/audio";
		final String outputDir = "/Users/ollie/Programming/Eclipse/Onset Detector Tests/output";
		SampleManager.group("audio", audioDir);
		
		for(int i = 0; i < SampleManager.getGroup("audio").size(); i++) {
			//set up audio
			final AudioContext ac = new AudioContext();
			//set up playback
			final Sample samp = SampleManager.fromGroup("audio", i);
			SamplePlayer player = new SamplePlayer(ac, samp);
			Gain sampleTrack = new Gain(ac, samp.nChannels);
			sampleTrack.addInput(player);
			ac.out.addInput(sampleTrack);
			//set up analysis
			SimplePowerOnsetDetector od = new SimplePowerOnsetDetector(ac);
			od.setThresholds(new float[] {0.05f, 0.1f});
			od.setCutout(100f);
			od.addInput(sampleTrack);
			ac.out.addDependent(od);
			//set up a listener and hi-hat track
			final Gain clickTrack = new Gain(ac, samp.nChannels);
			ac.out.addInput(clickTrack);
			od.addListener(new Bead() {
				public void messageReceived(Bead message) {
	//				SamplePlayer hat = new SamplePlayer(ac, samp);
	//				Gain rider = new Gain(ac, samp.nChannels, new Envelope(ac, 1f));
	//				((Envelope)rider.getGainEnvelope()).addSegment(0f, 100f, new KillTrigger(rider));
	//				rider.addInput(hat);
	//				clickTrack.addInput(rider);
					clickTrack.addInput(new Clicker(ac, 1f));
					System.out.println("click");
				}
			});
			//set up recording on the click track
			final Sample clickRecording = new Sample(ac.getAudioFormat(), (long)ac.msToSamples(samp.length));
			Recorder rec = new Recorder(ac, clickRecording);
			rec.addInput(clickTrack);
			ac.out.addDependent(rec);
			rec.setKillListener(new Bead() {
				public void messageReceived(Bead message) {
					try {
						System.out.println("done");
						clickRecording.write(outputDir + "/clicks_for_" + samp.getSimpleFileName() + ".aif");
						ac.stop();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			//start
			ac.runNonRealTime();
		}
	}
	
}
