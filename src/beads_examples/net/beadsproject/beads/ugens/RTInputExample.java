package net.beadsproject.beads.ugens;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.RTInput;


public class RTInputExample {
	
	public static void main(String[] args) throws LineUnavailableException, IOException {
		  AudioContext ac = new AudioContext(512, 1500, new AudioFormat(44100, 16, 2, true, true));
		  RTInput input = new RTInput(ac, new AudioFormat(44100, 16, 2, true, true));
		  ac.out.addInput(input);
		  ac.start();
	}

}
