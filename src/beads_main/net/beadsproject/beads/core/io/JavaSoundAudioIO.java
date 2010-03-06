/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.core.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
//import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.core.UGen;

public class JavaSoundAudioIO extends AudioIO {

	/** The default system buffer size. */
	public static final int DEFAULT_SYSTEM_BUFFER_SIZE = 5000;

	/** The mixer. */
	private Mixer mixer;

	/** The source data line. */
	private SourceDataLine sourceDataLine;

	/** The system buffer size in frames. */
	private int systemBufferSizeInFrames;

	/** Thread for running realtime audio. */
	private Thread audioThread;

	/** The priority of the audio thread. */
	private int threadPriority;

	/** The current byte buffer. */
	private byte[] bbuf;

	public JavaSoundAudioIO() {
		this(DEFAULT_SYSTEM_BUFFER_SIZE);
	}

	public JavaSoundAudioIO(int systemBufferSize) {
		this.systemBufferSizeInFrames = systemBufferSize;
		setThreadPriority(Thread.MAX_PRIORITY);
	}

	/**
	 * Initialises JavaSound.
	 */
	@Override
	public boolean create() {
		AudioFormat audioFormat = getContext().getAudioFormat();
		getDefaultMixerIfNotAlreadyChosen();
		if (mixer == null) {
			return false;
		}
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
		return true;
	}

	/**
	 * Gets the JavaSound mixer being used by this AudioContext.
	 * 
	 * @return the requested mixer.
	 */
	private void getDefaultMixerIfNotAlreadyChosen() {
		if (mixer == null) {
			selectMixer(0);
		}
	}

	/**
	 * Presents a choice of mixers on the commandline.
	 */
	public void chooseMixerCommandLine() {
		System.out.println("Choose a mixer...");
		printMixerInfo();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			selectMixer(Integer.parseInt(br.readLine()) - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Select a mixer by index.
	 * 
	 * @param i
	 *            the index of the selected mixer.
	 */
	public void selectMixer(int i) {
		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
		mixer = AudioSystem.getMixer(mixerinfo[i]);
		if (mixer != null) {
			System.out.print("JavaSoundAudioIO: Chosen mixer is ");
			System.out.println(mixer.getMixerInfo().getName() + ".");
		} else {
			System.out.println("JavaSoundAudioIO: Failed to get mixer.");
		}
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
			System.out.println((i + 1) + ") " + name + " --- "
					+ mixerinfo[i].getDescription());
			Mixer m = AudioSystem.getMixer(mixerinfo[i]);
			Line.Info[] lineinfo = m.getSourceLineInfo();
			for (int j = 0; j < lineinfo.length; j++) {
				System.out.println("  - " + lineinfo[j].toString());
			}
		}
	}

	/**
	 * Sets the priority of the audio thread. Default priority is
	 * Thread.MAX_PRIORITY.
	 * 
	 * @param priority
	 */
	public void setThreadPriority(int priority) {
		this.threadPriority = priority;
		if (audioThread != null)
			audioThread.setPriority(threadPriority);
	}

	/**
	 * @return The priority of the audio thread.
	 */
	public int getThreadPriority() {
		return this.threadPriority;
	}

	/** Shuts down JavaSound elements, SourceDataLine and Mixer. */
	@Override
	protected boolean destroy() {
		sourceDataLine.drain();
		sourceDataLine.stop();
		sourceDataLine.close();
		sourceDataLine = null;
		mixer.close();
		mixer = null;
		return true;
	}

	/** Starts the audio system running. */
	@Override
	protected boolean start() {
		audioThread = new Thread(new Runnable() {
			public void run() {
				// create JavaSound stuff only when needed
				create();
				// start the update loop
				runRealTime();
				// return from above method means context got stopped, so now
				// clean up
				destroy();
			}
		});
		audioThread.setPriority(threadPriority);
		audioThread.start();
		return true;
	}

	/** Update loop called from within audio thread (created in start() method). */
	private void runRealTime() {
		AudioContext context = getContext();
		AudioFormat audioFormat = context.getAudioFormat();
		int bufferSizeInFrames = context.getBufferSize();
		bbuf = new byte[bufferSizeInFrames * audioFormat.getFrameSize()];
		boolean isBigEndian = audioFormat.isBigEndian();
		int channels = audioFormat.getChannels();
		// float[] interleavedOutput = new float[audioFormat.getChannels() *
		// bufferSizeInFrames];
		sourceDataLine.start();
		while (context.isRunning()) {
			boolean goodFrame = update(); // this propagates update call to
			// context
			if (goodFrame) {
				if (isBigEndian) {
					for (int i = 0, counter = 0; i < bufferSizeInFrames; ++i) {
						for (int j = 0; j < channels; ++j) {
							short y = (short) (32767. * Math.min(Math.max(
									context.out.getValue(j, i), -1.0f), 1.0f));
							bbuf[counter++] = (byte) ((y >> 8) & 0xFF);
							bbuf[counter++] = (byte) (y & 0xFF);
						}
					}
				} else {
					for (int i = 0, counter = 0; i < bufferSizeInFrames; ++i) {
						for (int j = 0; j < channels; ++j) {
							short y = (short) (32767. * Math.min(Math.max(
									context.out.getValue(j, i), -1.0f), 1.0f));
							bbuf[counter++] = (byte) (y & 0xFF);
							bbuf[counter++] = (byte) ((y >> 8) & 0xFF);
						}
					}
				}
				sourceDataLine.write(bbuf, 0, bbuf.length);
			}

		}
	}

	@Override
	protected UGen getAudioInput(int[] channels) {
		// TODO not properly implemented, this does not respond to channels arg.
		return new JavaSoundRTInput(getContext(), getContext().getAudioFormat());
	}

	/**
	 * JavaSoundRTInput gathers audio from the JavaSound audio input device.
	 * 
	 * @beads.category input
	 */
	private class JavaSoundRTInput extends UGen {

		/** The audio format. */
		private AudioFormat audioFormat;

		/** The target data line. */
		private TargetDataLine targetDataLine;

		/** Flag to tell whether JavaSound has been initialised. */
		private boolean javaSoundInitialized;

		// private float[] interleavedSamples;
		private byte[] bbuf;

		private boolean isBigEndian;
		private int channels;

		/**
		 * Instantiates a new RTInput.
		 * 
		 * @param context
		 *            the AudioContext.
		 * @param audioFormat
		 *            the AudioFormat.
		 */
		JavaSoundRTInput(AudioContext context, AudioFormat audioFormat) {
			super(context, audioFormat.getChannels());
			this.audioFormat = audioFormat;
			isBigEndian = audioFormat.isBigEndian();
			channels = audioFormat.getChannels();
			javaSoundInitialized = false;
		}

		/**
		 * Set up JavaSound. Requires that JavaSound has been set up in
		 * AudioContext.
		 */
		public void initJavaSound() {
			DataLine.Info info = new DataLine.Info(TargetDataLine.class,
					audioFormat);
			try {
				int inputBufferSize = 5000;
				targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
				targetDataLine.open(audioFormat, inputBufferSize);
				if (targetDataLine == null)
					System.out.println("no line");
				else
					System.out.println("CHOSEN INPUT: "
							+ targetDataLine.getLineInfo()
							+ ", buffer size in bytes: " + inputBufferSize);
			} catch (LineUnavailableException ex) {
				System.out.println(getClass().getName()
						+ " : Error getting line\n");
			}
			targetDataLine.start();
			javaSoundInitialized = true;
			// interleavedSamples = new float[bufferSize *
			// audioFormat.getChannels()];
			bbuf = new byte[bufferSize * audioFormat.getFrameSize()];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.olliebown.beads.core.UGen#calculateBuffer()
		 */
		@Override
		public void calculateBuffer() {
			if (!javaSoundInitialized) {
				initJavaSound();
			}
			targetDataLine.read(bbuf, 0, bbuf.length);

			int ib = 0;

			if (isBigEndian) {
				for (int i = 0; i < bufferSize; ++i) {
					for (int j = 0; j < channels; ++j) {
						bufOut[j][i] = ((bbuf[ib] << 8) | (bbuf[ib + 1] & 0xFF)) / 32768.0F;
						ib += 2;
					}
				}
			} else {
				for (int i = 0; i < bufferSize; ++i) {
					for (int j = 0; j < channels; ++j) {
						bufOut[j][i] = ((bbuf[ib] & 0xFF) | (bbuf[ib + 1] << 8)) / 32768.0F;
						ib += 2;
					}
				}
			}

		}

	}

}
