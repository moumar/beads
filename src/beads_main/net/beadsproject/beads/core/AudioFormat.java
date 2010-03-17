package net.beadsproject.beads.core;

/**
 * Encapsulates information about the format of audio for 
 * classes such as {@link Sample} and {@link AudioFile}.
 * 
 * We have elected to use our own AudioFormat instead of 
 * javax.sound.sampled.AudioFormat as javasound is not supported everywhere.
 * 
 * NOTES: For now we just wrap javasound, but we can disconnect it soon
 * 
 * @author ben
 */
public class AudioFormat extends javax.sound.sampled.AudioFormat {

	public AudioFormat(Encoding arg0, float arg1, int arg2, int arg3, int arg4,
			float arg5, boolean arg6) {
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}
}
