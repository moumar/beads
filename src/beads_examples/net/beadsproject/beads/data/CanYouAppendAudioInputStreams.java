package net.beadsproject.beads.data;

import java.io.File;
import java.io.FileOutputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.sampled.file.AudioOutputStream;

public class CanYouAppendAudioInputStreams {

	public static void main(String[] args) throws Exception {
		AudioInputStream ais1 = AudioSystem.getAudioInputStream(new File("audio/1234.aif"));
		AudioInputStream ais2 = AudioSystem.getAudioInputStream(new File("audio/1234.aif"));
		
		FileOutputStream fos = new FileOutputStream(new File("/Users/ollie/Desktop/test.aif"));
		AudioSystem.write(ais1, AudioFileFormat.Type.AIFF, fos);
		AudioSystem.write(ais2, AudioFileFormat.Type.AIFF, fos);
		fos.close();
		
		AudioOutputStream aos;
		
	}
}
