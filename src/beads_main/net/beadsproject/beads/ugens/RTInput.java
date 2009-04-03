/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.core.UGen;

/**
 * RTInput gathers audio from an audio input device.
 */
public class RTInput extends UGen {

	/** The audio format. */
	private AudioFormat audioFormat;
	
	/** The target data line. */
	private TargetDataLine targetDataLine;
	
	/** Flag to tell whether JavaSound has been initialised. */
	private boolean javaSoundInitialized;
	
	private float[] interleavedSamples;
	private byte[] bbuf;

	/**
	 * Instantiates a new RTInput.
	 * 
	 * @param context
	 *            the AudioContext.
	 */
	public RTInput(AudioContext context) {
		//TODO grab the correct AudioFormat info from context.
		this(context, new AudioFormat(44100, 16, 1, true, true));
	}

	/**
	 * Instantiates a new RTInput.
	 * 
	 * @param context
	 *            the AudioContext.
	 * @param audioFormat
	 *            the AudioFormat.
	 */
	public RTInput(AudioContext context, AudioFormat audioFormat) {
		super(context, audioFormat.getChannels());
		this.audioFormat = audioFormat;
		javaSoundInitialized = false;
	}
	
	/**
	 * Set up JavaSound. Requires that JavaSound has been set up in AudioContext.
	 */
	public void initJavaSound() {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		try {
			int inputBufferSize = 4000;
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info); 
			targetDataLine.open(audioFormat, inputBufferSize);
			if(targetDataLine == null) System.out.println("no line");
			else System.out.println("CHOSEN INPUT: " + targetDataLine.getLineInfo() + ", buffer size in bytes: " + inputBufferSize);
		} catch (LineUnavailableException ex) {
			System.out.println(getClass().getName() + " : Error getting line\n");
		}
		targetDataLine.start();
		javaSoundInitialized = true;
		interleavedSamples = new float[bufferSize * audioFormat.getChannels()];
		bbuf = new byte[bufferSize * audioFormat.getFrameSize()];
	}
	

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		if(!javaSoundInitialized) {
			initJavaSound();
		}
		targetDataLine.read(bbuf, 0, bbuf.length);
		AudioUtils.byteToFloat(interleavedSamples, bbuf, audioFormat.isBigEndian());
		AudioUtils.deinterleave(interleavedSamples, audioFormat.getChannels(), bufferSize, bufOut);
	}


	
}
