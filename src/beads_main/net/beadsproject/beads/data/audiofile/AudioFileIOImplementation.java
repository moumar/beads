/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data.audiofile;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * The Class AudioFileIOImplementation provides methods for how the system
 * grabs an {@link AudioFile} from a file name or an {@link InputStream}. The
 * default is {@link JavaSoundAudioFileIOImplementation}.
 */
public abstract class AudioFileIOImplementation {
	
	/**
	 * Retrieve an audio file from a given filename String.
	 *
	 * @param filename the filename.
	 * @return the audio file
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedAudioFileException an unsupported audio file exception.
	 */
	public abstract AudioFile getAudioFile(String filename) throws IOException, UnsupportedAudioFileException;
	
	/**
	 * Retrieve an audio file from a given InputStream.
	 *
	 * @param is the InputStream.
	 * @return the audio file
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedAudioFileException an unsupported audio file exception.
	 */
	public abstract AudioFile getAudioFile(java.io.InputStream is) throws IOException, UnsupportedAudioFileException;

}
