package net.beadsproject.beads.data.audiofile;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

public class JavaSoundAudioFileIOImplementation extends AudioFileIOImplementation {
	
	@Override
	public AudioFile getAudioFile(String filename) throws IOException, UnsupportedAudioFileException {
		return new JavaSoundAudioFile(filename);
	}
	
	@Override
	public AudioFile getAudioFile(InputStream is) throws IOException, UnsupportedAudioFileException {
		return new JavaSoundAudioFile(is);
	}
	
}
