package net.beadsproject.beads.data.audiofile;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

public abstract class AudioFileIOImplementation {
	
	/**
	 * Retrieve an audio file. 
	 * 
	 * @param filename
	 * @return
	 */
	public abstract AudioFile getAudioFile(String filename) throws IOException, UnsupportedAudioFileException;
	
	/**
	 * Retrieve an audio file. 
	 * 
	 * @param input stream
	 * @return
	 */
	public abstract AudioFile getAudioFile(java.io.InputStream is) throws IOException, UnsupportedAudioFileException;

}
