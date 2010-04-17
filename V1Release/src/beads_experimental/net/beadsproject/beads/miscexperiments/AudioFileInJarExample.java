package net.beadsproject.beads.miscexperiments;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.AudioFile;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.SamplePlayer;

public class AudioFileInJarExample {

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
