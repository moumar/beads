/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data.audiofile;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * The Class JavaSoundAudioFileIOImplementation is the default {@link AudioFileIOImplementation}
 * and returns a {@link JavaSoundAudioFile}.
 */
public class JavaSoundAudioFileIOImplementation extends AudioFileIOImplementation {
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.data.audiofile.AudioFileIOImplementation#getAudioFile(java.lang.String)
	 */
	@Override
	public AudioFile getAudioFile(String filename) throws IOException, UnsupportedAudioFileException {
		return new JavaSoundAudioFile(filename);
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.data.audiofile.AudioFileIOImplementation#getAudioFile(java.io.InputStream)
	 */
	@Override
	public AudioFile getAudioFile(InputStream is) throws IOException, UnsupportedAudioFileException {
		return new JavaSoundAudioFile(is);
	}
	
}
