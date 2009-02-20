/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import net.beadsproject.beads.analysis.segmenters.SimplePowerOnsetDetector;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.SampleManager;

// TODO: Auto-generated Javadoc
/**
 * The Class RTInput.
 */
public class RTInput extends UGen {

	/** The audio format. */
	private AudioFormat audioFormat;
	
	/** The target data line. */
	private TargetDataLine targetDataLine;
	
	private boolean javaSoundInitialized;

	/**
	 * Instantiates a new rT input.
	 * 
	 * @param context
	 *            the context
	 */
	public RTInput(AudioContext context) {
		this(context, new AudioFormat(44100, 16, 1, true, true));
	}

	/**
	 * Instantiates a new rT input.
	 * 
	 * @param context
	 *            the context
	 * @param audioFormat
	 *            the audio format
	 */
	public RTInput(AudioContext context, AudioFormat audioFormat) {
		super(context, audioFormat.getChannels());
		this.audioFormat = audioFormat;
		javaSoundInitialized = false;
	}
	
	/**
	 * Setup.
	 */
	public void initJavaSound() {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info); 
			targetDataLine.open(audioFormat, 4000);
			if(targetDataLine == null) System.out.println("no line");
			else System.out.println("CHOSEN INPUT: " + targetDataLine.getLineInfo());
		} catch (LineUnavailableException ex) {
			System.out.println(getClass().getName() + " : Error getting line\n");
		}
		targetDataLine.start();
		javaSoundInitialized = true;
	}
	

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		if(!javaSoundInitialized) {
			initJavaSound();
		}
		byte[] bbuf = new byte[bufferSize * audioFormat.getFrameSize()];
		targetDataLine.read(bbuf, 0, bbuf.length);
		float[] interleavedSamples = new float[bufferSize * audioFormat.getChannels()];
		AudioUtils.byteToFloat(interleavedSamples, bbuf, audioFormat.isBigEndian());
		AudioUtils.deinterleave(interleavedSamples, audioFormat.getChannels(), bufferSize, bufOut);
	}


	
}
