package net.beadsproject.beads.data;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.SamplePlayer;

public class SampleGradualLoadExample {

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		
		//find a long file to play back
		//String pathToFile = "/Users/ollie/Music/Audio/Igor/Petrouchka.aif";
		//String pathToFile = "/Users/ollie/Programming/Eclipse/BeadsTests/audio/gammaBrosTheme.mp3";
		//String pathToFile = "../BeadsTests/audio/gammaBrosTheme.mp3.aif";
		String pathToFile = "../BeadsTests/audio/gammaBrosTheme.mp3";
		
		//example settings: TIMED, regionsize 100, lookahead 2000, lookback 2000, memory 5000.
		//BP: Fixed a few problems, added new loading regime, seems to work okay now...
		
		AudioContext ac = new AudioContext();
		final Sample samp = new Sample();

		//-------------------------
		//Sample buffering settings
		samp.setBufferingRegime(Sample.BufferingRegime.TIMED);
		//samp.setBufferingRegime(Sample.BufferingRegime.TOTAL);
		//only apply to the timed regime
		samp.setRegionSize(100);
		samp.setLookAhead(2000);
		samp.setLookBack(2000);
		samp.setMemory(5000);
		//-------------------------
		AudioFile af = new AudioFile(pathToFile);
		af.trace = true; // trace access ops
		samp.setFile(af);				
		
		final SamplePlayer play = new SamplePlayer(ac, samp);
		play.setKillOnEnd(false);
		ac.out.addInput(play);
		
		//make a little skipping tool
		final Clock clock = new Clock(ac, 500f);
		ac.out.addDependent(clock);
		clock.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				if(clock.isBeat()) {
					float direction = Math.random() < 0.5 ? 1f : -1f;
					double pos = samp.getLength() * Math.random();
					System.out.println("new pos: " + pos + ", direction: " + direction);
					play.setPosition(pos);
					play.getRateEnvelope().setValue(direction);
					
				}
			}
		});
		
		ac.start();
	}
}
