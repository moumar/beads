package net.beadsproject.beads.ugens;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.core.Gain;
import net.beadsproject.beads.ugens.effect.TapIn;
import net.beadsproject.beads.ugens.effect.TapOut;


public class RTInputExample {
	
	public static void main(String[] args) throws LineUnavailableException, IOException {
		  AudioContext ac = new AudioContext();
//		  RTInput input = new RTInput(ac, new AudioFormat(44100, 16, 2, true, true));
		  UGen input = ac.getAudioInput(new int[] {0});
		  //RTInput input = new RTInput(ac);
		  ac.out.addInput(input);
		  TapIn tin = new TapIn(ac, 10000f);
		  tin.addInput(input);
		  for(int i = 0; i < 10; i++) {
			  float delay = (float)Math.random() * 1000f;
			  TapOut tout = new TapOut(ac, tin, delay);
			  Gain g = new Gain(ac, 1, 0.5f * (float)Math.pow((1f - delay / 1000f), 2f));
			  g.addInput(tout);
			  ac.out.addInput(g);
		  }
		  ac.start();
	}

}
