/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 * CREDIT: This class uses portions of code taken from JASS. See readme/CREDITS.txt.
 * 
 */
package net.beadsproject.beads.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Recorder;

/**
 * AudioContext provides the core audio set up for running audio in a Beads
 * project. An AudioContext determines the JavaSound {@link AudioFormat} used,
 * the IO device, the audio buffer size and the system IO buffer size. An
 * AudioContext also has provides a {@link UGen} called {@link #out}, which is
 * the output point for networks of UGens in a Beads project.
 * 
 * @beads.category control
 * @author ollie
 */
public class AudioContext {

	public static final int DEFAULT_BUFFER_SIZE = 512;

	/** The audio IO device. */
	private AudioIO audioIO;

	/** The audio format. */
	private AudioFormat audioFormat;

	/** The stop flag. */
	private boolean stopped;

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

	/** Used for allocating buffers to UGens. */
	private int maxReserveBufs;
	private ArrayList<float[]> bufferStore;
	private int bufStoreIndex;
	private float[] zeroBuf;

	/** Used for testing for dropped frames. */
	private long nanoLeap;
	private long nanoStart;
	private boolean lastFrameGood;

	/**
	 * Creates a new AudioContext with default settings. The default buffer size
	 * is 512 and the default system buffer size is 5000. The default audio
	 * format is 44.1Khz, 16 bit, stereo, signed, bigEndian.
	 */
	public AudioContext() {
		this(DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Creates a new AudioContext with default settings and the specified buffer
	 * size. The default system buffer size is determined by the JVM. The
	 * default audio format is 44.1Khz, 16 bit, stereo, signed, bigEndian.
	 * 
	 * @param bufferSizeInFrames
	 *            the buffer size in samples.
	 */
	public AudioContext(int bufferSizeInFrames) {
		this(bufferSizeInFrames, defaultAudioIO());
	}

	/**
	 * Creates a new AudioContext with default audio format and the specified
	 * buffer size and system buffer size. The default audio format is 44.1Khz,
	 * 16 bit, stereo, signed, bigEndian.
	 * 
	 * @param bufferSizeInFrames
	 *            the buffer size in samples.
	 * @param systemBufferSizeInFrames
	 *            the system buffer size in samples.
	 */
	public AudioContext(int bufferSizeInFrames, AudioIO ioSystem) {
		// use almost entirely default settings
		this(bufferSizeInFrames, ioSystem, defaultAudioFormat(2));
	}

	//TODO - note - Benito added this constructor
	/**
	 * Creates a new AudioContext with the default system buffer size and the
	 * specified audio format and buffer size.
	 * 
	 * @param audioFormat
	 *            the audio format, which specifies sample rate, bit depth,
	 *            number of channels, signedness and byte order.
	 */
	public AudioContext(int bufferSizeInFrames, AudioFormat audioFormat) {
		this(bufferSizeInFrames, defaultAudioIO(), audioFormat);
	}

	/**
	 * Creates a new AudioContext with default buffer size, default system
	 * buffer size and the specified audio format.
	 * 
	 * @param audioFormat
	 *            the audio format, which specifies sample rate, bit depth,
	 *            number of channels, signedness and byte order.
	 */
	public AudioContext(AudioFormat audioFormat) {
		this(DEFAULT_BUFFER_SIZE, defaultAudioIO(), audioFormat);
	}

	/**
	 * Creates a new AudioContext with the specified buffer size, system buffer
	 * size and audio format.
	 * 
	 * @param bufferSizeInFrames
	 *            the buffer size in samples.
	 * @param systemBufferSizeInFrames
	 *            the system buffer size in samples.
	 * @param audioFormat
	 *            the audio format, which specifies sample rate, bit depth,
	 *            number of channels, signedness and byte order.
	 */
	public AudioContext(int bufferSizeInFrames, AudioIO audioIO,
			AudioFormat audioFormat) {
		// set up basic stuff
		checkForDroppedFrames = true;
		logTime = false;
		maxReserveBufs = 50;
		stopped = true;
		// set audio format
		this.audioFormat = audioFormat;
		// set buffer size
		setBufferSize(bufferSizeInFrames);
		// set up the default root UGen
		out = new Gain(this, audioFormat.getChannels());
		// bind to AudioIO
		this.audioIO = audioIO;
		this.audioIO.context = this;
		this.audioIO.prepare();
	}

	/**
	 * Returns a UGen which can be used to grab audio from the audio input, as
	 * specified by the AudioIO.
	 * 
	 * @param channels
	 *            an array of ints indicating which channels are required.
	 * @return a UGen which can be used to access audio input.
	 */
	public UGen getAudioInput(int[] channels) {
		return audioIO.getAudioInput(channels);
	}

	/**
	 * produces the default AudioIO which is a JavaSoundAudioIO with noargs
	 * constructor.
	 * 
	 * @return a new JavaSoundAudioIO with noargs constructor.
	 */
	public static AudioIO defaultAudioIO() {
		return new JavaSoundAudioIO();
	}

	/**
	 * Sets up the reserve of buffers.
	 */
	private void setupBufs() {
		bufferStore = new ArrayList<float[]>();
		while (bufferStore.size() < maxReserveBufs) {
			bufferStore.add(new float[bufferSizeInFrames]);
		}
		zeroBuf = new float[bufferSizeInFrames];
	}

	/** callback from AudioIO. */
	protected boolean update() {
		if (lastFrameGood) {
			bufStoreIndex = 0;
			Arrays.fill(zeroBuf, 0f);
			out.update(); // this will propagate all of the updates
		}
		// now check for dropped frames
		// the timeStep condition is a bit of a fiddle,
		// seems there's always dropped frames to being with
		if (timeStep > 500 && checkForDroppedFrames) {
			long expectedNanoTime = nanoLeap * (timeStep + 1);
			long realNanoTime = System.nanoTime() - nanoStart;
			float frameDifference = (float) (expectedNanoTime - realNanoTime)
					/ (float) nanoLeap;
			if (frameDifference < -1) {
				lastFrameGood = false;
				System.out.println("Audio dropped frame.");
			} else
				lastFrameGood = true;
		}
		timeStep++;
		if (Thread.interrupted())
			System.out.println("Thread interrupted");
		if (logTime && timeStep % 100 == 0) {
			System.out.println(samplesToMs(timeStep * bufferSizeInFrames)
					/ 1000f + " (seconds), bufferStore.size()="
					+ bufferStore.size());
		}
		return lastFrameGood;
	}

	/**
	 * Gets a buffer from the buffer reserve. This buffer will be owned by you
	 * until the next time step, and you shouldn't attempt to use it outside of
	 * the current time step. The length of the buffer is bufferSize, but there
	 * is no guarantee as to its contents.
	 * 
	 * @return buffer of size bufSize, unknown contents.
	 */
	public float[] getBuf() {
		if (bufStoreIndex < bufferStore.size()) {
			return bufferStore.get(bufStoreIndex++);
		} else {
			float[] buf = new float[bufferSizeInFrames];
			bufferStore.add(buf);
			bufStoreIndex++;
			return buf;
		}
	}

	/**
	 * Gets a zero initialised buffer from the buffer reserve. This buffer will
	 * be owned by you until the next time step, and you shouldn't attempt to
	 * use it outside of the current time step. The length of the buffer is
	 * bufferSize, and the buffer is full of zeros.
	 * 
	 * @return buffer of size bufSize, all zeros.
	 */
	public float[] getCleanBuf() {
		float[] buf = getBuf();
		Arrays.fill(buf, 0f);
		return buf;
	}

	/**
	 * Gets a pointer to a buffer of length bufferSize, full of zeros. Changing
	 * the contents of this buffer would be completely disastrous. If you want a
	 * buffer of zeros that you can actually do something with, use {@link
	 * getCleanBuf()}.
	 * 
	 * @return buffer of size bufSize, all zeros.
	 */
	public float[] getZeroBuf() {
		return zeroBuf;
	}

	/**
	 * Starts the AudioContext running in non-realtime. This occurs in the
	 * current Thread.
	 */
	public void runNonRealTime() {
		if (stopped) {
			stopped = false;
			while (out != null && !stopped) {
				bufStoreIndex = 0;
				Arrays.fill(zeroBuf, 0f);
				if (!out.isPaused())
					out.update();
				timeStep++;
				if (logTime && timeStep % 100 == 0) {
					System.out.println(samplesToMs(timeStep
							* bufferSizeInFrames)
							/ 1000f + " (seconds)");
				}
			}
		}
	}

	/**
	 * Runs the AudioContext in non-realtime for n milliseconds (that's n
	 * non-realtime milliseconds).
	 * 
	 * @param n
	 *            number of milliseconds.
	 */
	public void runForNMillisecondsNonRealTime(float n) {
		// time the playback to n seconds
		DelayTrigger dt = new DelayTrigger(this, n,
				new AudioContextStopTrigger(this));
		out.addDependent(dt);
		runNonRealTime();
	}

	/**
	 * Sets the buffer size.
	 * 
	 * @param bufferSize
	 *            the new buffer size.
	 */
	private void setBufferSize(int bufferSize) {
		bufferSizeInFrames = bufferSize;
		setupBufs();
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

	/**
	 * Generates a new AudioFormat with the same everything as the
	 * AudioContext's AudioFormat except for the number of channels.
	 * 
	 * @param numChannels
	 *            the number of channels.
	 * @return a new AudioFormat with the given number of channels, all other
	 *         properties coming from the original AudioFormat.
	 */
	public AudioFormat getAudioFormat(int numChannels) {
		AudioFormat newFormat = new AudioFormat(audioFormat.getEncoding(),
				audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits(),
				numChannels, audioFormat.getFrameSize(), audioFormat
						.getFrameRate(), audioFormat.isBigEndian());
		return newFormat;
	}

	/**
	 * Generates the default {@link AudioFormat} for AudioContext, with the
	 * given number of channels. The default values are: sampleRate=44100,
	 * sampleSizeInBits=16, signed=true, bigEndian=true.
	 * 
	 * @param numChannels
	 *            the number of channels to use.
	 * @return the generated AudioFormat.
	 */
	public static AudioFormat defaultAudioFormat(int numChannels) {
		return new AudioFormat(44100, 16, numChannels, true, true);
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
	 * Prints a representation of the audio signal chain stemming upwards from
	 * the specified UGen to System.out, indented by the specified depth.
	 * 
	 * @param current
	 *            UGen to start from.
	 * @param depth
	 *            depth by which to indent.
	 */
	public static void printCallChain(UGen current, int depth) {
		Set<UGen> children = current.getConnectedInputs();
		for (int i = 0; i < depth; i++) {
			System.out.print("  ");
		}
		System.out.println("- " + current);
		for (UGen child : children) {
			printCallChain(child, depth + 1);
		}
	}

	/**
	 * Prints the entire call chain to System.out (equivalent to
	 * AudioContext.printCallChain(this.out, 0);)
	 */
	public void printCallChain() {
		AudioContext.printCallChain(out, 0);
	}

	/**
	 * Converts samples to milliseconds at the current sample rate.
	 * 
	 * @param msTime
	 *            duration in milliseconds.
	 * 
	 * @return number of samples.
	 */
	public double msToSamples(double msTime) {
		return msTime * (audioFormat.getSampleRate() / 1000.0);
	}

	/**
	 * Converts milliseconds to samples at the current sample rate.
	 * 
	 * @param sampleTime
	 *            number of samples.
	 * 
	 * @return duration in milliseconds.
	 */
	public double samplesToMs(double sampleTime) {
		return (sampleTime / audioFormat.getSampleRate()) * 1000.0;
	}

	/**
	 * Gets the current time step of this AudioContext. The time step begins at
	 * zero when the AudioContext is started and is incremented by 1 for each
	 * update of the audio buffer.
	 * 
	 * @return current time step.
	 */
	public long getTimeStep() {
		return timeStep;
	}

	/**
	 * Generates a TimeStamp with the current time step and the given index into
	 * the time step.
	 * 
	 * @param index
	 *            the index into the current time step.
	 * @return a TimeStamp.
	 */
	public TimeStamp generateTimeStamp(int index) {
		return new TimeStamp(this, timeStep, index);
	}

	/**
	 * Get the runtime (in ms) since starting.
	 */
	public double getTime() {
		return samplesToMs(getTimeStep() * getBufferSize());
	}

	/**
	 * Switch on/off logging of time when running in realtime. The time is
	 * printed to System.out every 100 time steps.
	 * 
	 * @param logTime
	 *            set true to log time.
	 */
	public void logTime(boolean logTime) {
		this.logTime = logTime;
	}

	/**
	 * Switch on/off checking for dropped frames when running in realtime. The
	 * scheduler checks to see if audio is not being calculated quickly enough,
	 * and prints dropped-frame messages to System.out.
	 * 
	 * @param checkForDroppedFrames
	 *            set true to check for dropped frames.
	 */
	public void checkForDroppedFrames(boolean checkForDroppedFrames) {
		this.checkForDroppedFrames = checkForDroppedFrames;
	}

	/**
	 * Tells the AudioContext to record all output for the given millisecond
	 * duration, kill the AudioContext, and save the recording to the given file
	 * path. This is a convenient way to make quick recordings, but may not suit
	 * every circumstance.
	 * 
	 * @param timeMS
	 *            the time in milliseconds to record for.
	 * @param filename
	 *            the filename to save the recording to.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * 
	 * @see Recorder recorder
	 * @see Sample sample
	 **/
	public void record(double timeMS, String filename) throws IOException {
		Sample s = new Sample(getAudioFormat(), (int) timeMS);
		Recorder r;
		try {
			r = new Recorder(this, s);
			r.addInput(out);
			out.addDependent(r);
			r.start();
			r.setKillListener(new AudioContextStopTrigger(this));
		} catch (Exception e) { /* won't happen */
		}

		while (isRunning()) {
		}
		s.write(filename);
	}

	/**
	 * Convenience method to quickly audition a {@link UGen}.
	 * 
	 * @param ugen
	 *            the {@link UGen} to audition.
	 */
	public void quickie(UGen ugen) {
		out.addInput(ugen);
		start();
	}

	/**
	 * Starts the AudioContext running in realtime. Only happens if not already
	 * running. Resets time.
	 */
	public void start() {
		if (stopped) {
			// calibration test stuff
			nanoStart = System.nanoTime();
			nanoLeap = (long) (1000000000 / (audioFormat.getSampleRate() / (float) bufferSizeInFrames));
			lastFrameGood = true;
			// reset time step
			timeStep = 0;
			stopped = false;
			// the AudioIO is where the thread actually runs.
			audioIO.start();
		}
	}

	/**
	 * Stops the AudioContext if running either in realtime or non-realtime.
	 */
	public void stop() {
		stopped = true;
		audioIO.stop();
	}

	/**
	 * Checks if this AudioContext is running.
	 * 
	 * @return true if running.
	 */
	public boolean isRunning() {
		return !stopped;
	}

}
