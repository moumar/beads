package net.beadsproject.beads.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.SamplePlayer;


public class AudioFileInJarTest {

	public static void main(String[] args) throws Exception {
		AudioFile af = new AudioFile(ClassLoader.getSystemResourceAsStream("audio/1234.aif"));
//		AudioFile af = new AudioFile(new FileInputStream(new File("audio/1234.aif")));
		Sample s = new Sample(af);
		AudioContext ac = new AudioContext();
		SamplePlayer sp = new SamplePlayer(ac, s);
		ac.out.addInput(sp);
		ac.start();
	}
}
