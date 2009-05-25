/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 * CREDIT: This class uses portions of code taken from JASS. See readme/CREDITS.txt.
 * 
 */
package net.beadsproject.beads.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Recorder;

/**
 * AudioContext provides the core audio set up for running audio in a Beads project. An
 * AudioContext determines the JavaSound {@link AudioFormat} used, the IO device, the
 * audio buffer size and the system IO buffer size. An AudioContext also has provides a {@link UGen} called {@link #out}, which is the output point for networks of UGens in a Beads
 * project.
 * 
 * @author ollie
 */
public class AudioContext {

	/** The audio format. */
	private AudioFormat audioFormat;

	/** The source data line. */
	private SourceDataLine sourceDataLine;
	
	/** The mixer. */
	private Mixer mixer;
	
	/** The buffer size in bytes. */
	private int bufferSizeInBytes;
	
	/** The current byte buffer. */
	private byte[] bbuf;
	
	/** Thread for running realtime audio. */
	private Thread audioThread;
	
	/** The stop flag. */
	private boolean stop;
	
	/** The root {@link UGen}. */
	public final Gain out;
	
	/** Flag for checking for dropped frames. */
	private boolean checkForDroppedFrames;
	
	/** The current time step. */
	private long timeStep; 
	
	/** Flag for logging time to System.out. */
	private boolean logTime;

	/** The buffer size in frames. */
	private int bufferSizeInFrames;
	
	/** The system buffer size in frames. */
	private int systemBufferSizeInFrames;

	/**
	 * Creates a new AudioContext with default settings. The default buffer size
	 * is 512 and the default system buffer size is 5000. The
	 * default audio format is 44.1Khz, 16 bit, stereo, signed, bigEndian.
	 */
	public AudioContext() {
		// use entirely default settings
		this(512);
	}

	/**
	 * Creates a new AudioContext with default settings and the specified buffer
	 * size. The default system buffer size is determined by the JVM. The
	 * default audio format is 44.1Khz, 16 bit, stereo, signed, bigEndian.
	 * 
	 * @param bufferSizeInFrames the buffer size in samples.
	 */
	public AudioContext(int bufferSizeInFrames) {
		this(bufferSizeInFrames, 5000);
	}

	/**
	 * Creates a new AudioContext with default audio format and the specified
	 * buffer size and system buffer size. The default audio format is 44.1Khz,
	 * 16 bit, stereo, signed, bigEndian.
	 * 
	 * @param bufferSizeInFrames the buffer size in samples.
	 * @param systemBufferSizeInFrames the system buffer size in samples.
	 */
	public AudioContext(int bufferSizeInFrames, int systemBufferSizeInFrames) {
		// use almost entirely default settings
		this(bufferSizeInFrames, systemBufferSizeInFrames, new AudioFormat(44100, 16, 2, true, true));
	}

	/**
	 * Creates a new AudioContext with the specified buffer size, system buffer
	 * size and audio format.
	 * 
	 * @param bufferSizeInFrames the buffer size in samples.
	 * @param systemBufferSizeInFrames the system buffer size in samples.
	 * @param audioFormat the audio format, which specifies sample rate, bit depth,
	 * number of channels, signedness and byte order.
	 */
	public AudioContext(int bufferSizeInFrames, int systemBufferSizeInFrames, AudioFormat audioFormat) {
		// set up other basic stuff
		mixer = null;
		stop = true;
		checkForDroppedFrames = true;
		logTime = false;
		// set audio format
		this.audioFormat = audioFormat;
		// set buffer size
		setBufferSize(bufferSizeInFrames);
		this.systemBufferSizeInFrames = systemBufferSizeInFrames;
		// set up the default root UGen
		out = new Gain(this, audioFormat.getChannels());
	}

	/**
	 * Initialises JavaSound.
	 */
	private void initJavaSound() {
		getDefaultMixerIfNotAlreadyChosen();
		System.out.print("CHOSEN MIXER: ");
		System.out.println(mixer.getMixerInfo().getName());
		if (mixer == null)
			return;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		try {
			sourceDataLine = (SourceDataLine) mixer.getLine(info);
			if (systemBufferSizeInFrames < 0)
				sourceDataLine.open(audioFormat);
			else
				sourceDataLine.open(audioFormat, systemBufferSizeInFrames
						* audioFormat.getFrameSize());
		} catch (LineUnavailableException ex) {
			System.out
					.println(getClass().getName() + " : Error getting line\n");
		}
	}

	/**
	 * Gets the JavaSound mixer being used by this AudioContext.
	 * 
	 * @return the requested mixer.
	 */
	private void getDefaultMixerIfNotAlreadyChosen() {
		if(mixer == null) {
			selectMixer(0);
		} 
	}
	
	
	/**
	 * Presents a choice of mixers on the commandline.
	 */
	public void chooseMixerCommandLine() {
		System.out.println("Choose a mixer....");
		printMixerInfo();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			selectMixer(Integer.parseInt(br.readLine()) - 1);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Select a mixer by index.
	 * 
	 * @param i the index of the selected mixer.
	 */
	public void selectMixer(int i) {
		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
		mixer = AudioSystem.getMixer(mixerinfo[i]);
	}

	/**
	 * Prints information about the current Mixer to System.out.
	 */
	public static void printMixerInfo() {
		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerinfo.length; i++) {
			String name = mixerinfo[i].getName();
			if (name.equals(""))
				name = "No name";
			System.out.println((i+1) + ") " + name + " --- " + mixerinfo[i].getDescription());
			Mixer m = AudioSystem.getMixer(mixerinfo[i]);
			Line.Info[] lineinfo = m.getSourceLineInfo();
			for (int j = 0; j < lineinfo.length; j++) {
				System.out.println("  - " + lineinfo[j].toString());
			}
		}
	}
	
	/**
	 * Runs the system in realtime.
	 */
	private void run() {
		runRealTime();
	}

	/**
	 * Used by this AudioContext's Thread, use {@link #start()} instead of this.
	 */
	private void runRealTime() {
		initJavaSound();
		sourceDataLine.start();
		// calibration test stuff
		long nanoStart = System.nanoTime();
		long nanoLeap = (long) (1000000000 / (audioFormat.getSampleRate() / (float) bufferSizeInFrames));
		boolean skipFrame = false;
		byte b = 0;
		Arrays.fill(bbuf, b);
		timeStep = 0;
		float[] interleavedOutput = new float[audioFormat.getChannels() * bufferSizeInFrames];
		while (!stop) {
			if (!skipFrame) {
				synchronized(this) {	//This was causing permanent freezing
					out.update(); // this will propagate all of the updates
					interleave(out.bufOut, interleavedOutput);
					AudioUtils.floatToByte(bbuf, interleavedOutput,
							audioFormat.isBigEndian());
				}
				sourceDataLine.write(bbuf, 0, bbuf.length);
			}
			if (checkForDroppedFrames) {
				long expectedNanoTime = nanoLeap * (timeStep + 1);
				long realNanoTime = System.nanoTime() - nanoStart;
				float frameDifference = (float) (expectedNanoTime - realNanoTime)
						/ (float) nanoLeap;
				if (frameDifference < -1) {
					skipFrame = true;
					System.out.println("Audio dropped frame.");
				} else
					skipFrame = false;
			}
			timeStep++;
			if(Thread.interrupted()) System.out.println("Thread interrupted");
			if(logTime && timeStep % 100 == 0) {
				System.out.println(samplesToMs(timeStep * bufferSizeInFrames) / 1000f + " (seconds)");
			}
		}
		sourceDataLine.drain();
		sourceDataLine.stop();
		sourceDataLine.close();
		sourceDataLine = null;
		mixer = null;
	}
	
	/**
	 * Checks if this AudioContext is running.
	 * 
	 * @return true if running.
	 */
	public boolean isRunning() {
		return !stop;
	}
	
	/**
	 * Starts the AudioContext running in realtime.
	 */
	public void start() {
		if(stop) {
			stop = false;
			audioThread = new Thread(new Runnable() {
				public void run() {
					AudioContext.this.run();
				}
			});
			audioThread.setPriority(Thread.MAX_PRIORITY);
			audioThread.start();
		}
	}

	/**
	 * Starts the AudioContext running in non-realtime. This occurs in the current Thread. 
	 */
	public void runNonRealTime() {
		if(stop) {
			stop = false;
			while (out != null && !stop) {
				if (!out.isPaused())
					out.update();
				timeStep++;
				if(logTime && timeStep % 100 == 0) {
					System.out.println(samplesToMs(timeStep * bufferSizeInFrames) / 1000f + " (seconds)");
				}
			}
		}
	}
	
	public void runForNSecondsNonRealTime(float n)
	{
		//time the playback to n seconds
		DelayTrigger dt = new DelayTrigger(this, n*1000f, new AudioContextStopTrigger(this));
		out.addDependent(dt);
		runNonRealTime();		
	}

	/**
	 * Interleaves a multi-channel audio buffer into a single interleaved buffer.
	 * 
	 * @param source the source.
	 * @param result the result.
	 */
	private void interleave(float[][] source, float[] result) {
		for (int i = 0, counter = 0; i < bufferSizeInFrames; ++i) {
			for (int j = 0; j < audioFormat.getChannels(); ++j) {
				result[counter++] = source[j][i];
			}
		}
	}

	/**
	 * Sets the buffer size.
	 * 
	 * @param bufferSize the new buffer size.
	 */
	private void setBufferSize(int bufferSize) {
		bufferSizeInFrames = bufferSize;
		bufferSizeInBytes = bufferSizeInFrames * audioFormat.getFrameSize();
		bbuf = new byte[bufferSizeInBytes];
	}

	/**
	 * Gets the buffer size for this AudioContext.
	 * 
	 * @return Buffer size in samples.
	 */
	public int getBufferSize() {
		return bufferSizeInFrames;
	}
	

	/**
	 * Gets the system buffer size for this AudioContext.
	 * 
	 * @return System buffer size in samples.
	 */
	public int getSystemBufferSize() {
		return systemBufferSizeInFrames;
	}

	/**
	 * Gets the sample rate for this AudioContext.
	 * 
	 * @return sample rate in samples per second.
	 */
	public float getSampleRate() {
		return audioFormat.getSampleRate();
	}

	/**
	 * Gets the AudioFormat for this AudioContext.
	 * 
	 * @return AudioFormat used by this AudioContext.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}
	
//	public AudioFormat copyOfAudioFormatWithChannels(int numChannels) {
//		AudioFormat newFormat = new AudioFormat(audioFormat.getSampleRate(),
//												audioFormat.getSampleSizeInBits(),
//												numChannels,
//												audioFormat.);
//	}

	/**
	 * Stops the AudioContext if running either in realtime or non-realtime.
	 */
	public void stop() {
		stop = true;
	}

	/**
	 * Prints AudioFormat information to System.out.
	 */
	public void postAudioFormatInfo() {
		System.out.println("Sample Rate: " + audioFormat.getSampleRate());
		System.out.println("Channels: " + audioFormat.getChannels());
		System.out
				.println("Frame size in Bytes: " + audioFormat.getFrameSize());
		System.out.println("Encoding: " + audioFormat.getEncoding());
		System.out.println("Big Endian: " + audioFormat.isBigEndian());
	}

	/**
	 * Prints SourceDataLine info to System.out.
	 */
	public void postSourceDataLineInfo() {
		System.out.println("----------------");
		System.out.println("buffer: " + (sourceDataLine.getBufferSize()));
		System.out
				.println("spare: "
						+ (sourceDataLine.getBufferSize() - sourceDataLine
								.available()));
		System.out.println("available: " + sourceDataLine.available());
	}

	/**
	 * Converts samples to milliseconds at the current sample rate.
	 * 
	 * @param msTime duration in milliseconds.
	 * 
	 * @return number of samples.
	 */
	public double msToSamples(double msTime) {
		return msTime * (audioFormat.getSampleRate() / 1000.0);
	}

	/**
	 * Converts milliseconds to samples at the current sample rate.
	 * 
	 * @param sampleTime number of samples.
	 * 
	 * @return duration in milliseconds.
	 */
	public double samplesToMs(double sampleTime) {
		return (sampleTime / audioFormat.getSampleRate()) * 1000.0;
	}

	/**
	 * Gets the current time step of this AudioContext. The time step begins at
	 * zero when the AudioContext is started and is incremented by 1 for each update of the audio buffer.
	 * 
	 * @return current time step.
	 */
	public long getTimeStep() {
		return timeStep;
	}
	
	/**
	 * Generates a TimeStamp with the current time step and the given index into the time step.
	 * @param index the index into the current time step.
	 * @return a TimeStamp.
	 */
	public TimeStamp generateTimeStamp(int index) {
		return new TimeStamp(this, timeStep, index);
	}
	
	/**
	 * Get the runtime (in ms) since start() 
	 */
	public double getTime(){
		return samplesToMs(getTimeStep()*getBufferSize());
	}

	/**
	 * Switch on/off logging of time when running in realtime. The time is printed to System.out every 100 time steps.
	 * 
	 * @param logTime set true to log time.
	 */
	public void logTime(boolean logTime) {
		this.logTime = logTime;
	}
	
	/**
	 * Switch on/off checking for dropped frames when running in realtime. The scheduler checks to see if audio is not being calculated quickly enough, and prints dropped-frame messages to System.out.
	 * 
	 * @param checkForDroppedFrames set true to check for dropped frames.
	 */
	public void checkForDroppedFrames(boolean checkForDroppedFrames) {
		this.checkForDroppedFrames = checkForDroppedFrames;
	}
	
	/**
	 * Tells the AudioContext to record all output for the given millisecond duration and save the recording to the given file path. This is a convenient way to make quick recordings, but may not suit every circumstance.
	 * 
	 * @param timeMS the time in milliseconds to record for.
	 * @param filename the filename to save the recording to.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * 
	 * @see {@link Recorder, Sample}
	 */
	public void record(double timeMS, String filename) throws IOException {
		Sample s = new Sample(getAudioFormat(), (int)msToSamples(timeMS));
		Recorder r = new Recorder(this, s);
		r.addInput(out);
		out.addDependent(r);
		r.start();
		r.setKillListener(new AudioContextStopTrigger(this));
		while(isRunning()) {}
		s.write(filename);
	}

	/**
	 * Convenience method to quickly audition a {@link UGen}.
	 * 
	 * @param ugen the {@link UGen} to audition.
	 */
	public void quickie(UGen ugen) {
		out.addInput(ugen);
		start();
	}

}
