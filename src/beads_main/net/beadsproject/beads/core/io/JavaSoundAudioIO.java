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
	
	/** The target data line. */
	private TargetDataLine targetDataLine;

	/** The system buffer size in frames. */
	private int systemBufferSizeInFrames;

	/** Thread for running realtime audio. */
	private Thread audioThread;

	/** The priority of the audio thread. */
	private int threadPriority;

	/** The current output byte buffer. */
	private byte[] bbufOut;
	
	/** The current input byte buffer. */
	private byte[] bbufIn;
	
	/** Used to determine whether inputs need configuring. */
	private boolean hasInput;
	

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
	private boolean setupOutputJavaSound() {
		AudioFormat audioFormat = getContext().getAudioFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		try {
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
			if (systemBufferSizeInFrames < 0)
				sourceDataLine.open(audioFormat);
			else
				sourceDataLine.open(audioFormat, systemBufferSizeInFrames
						* audioFormat.getFrameSize());
			System.out.println("JavaSoundAudioIO: Chosen output is "
					+ sourceDataLine.getLineInfo()
					+ ", buffer size in bytes: " + systemBufferSizeInFrames);
		} catch (LineUnavailableException ex) {
			System.out
					.println(getClass().getName() + " : Error getting line\n");
		}
		return true;
	}

	private void setupInputJavaSound() {
		AudioFormat audioFormat = context.getInputAudioFormat();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
				audioFormat);
		try {
			int inputBufferSize = systemBufferSizeInFrames * audioFormat.getFrameSize();
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat, inputBufferSize);
			if (targetDataLine == null)
				System.out.println("no line");
			else
				System.out.println("JavaSoundAudioIO: Chosen input is "
						+ targetDataLine.getLineInfo()
						+ ", buffer size in bytes: " + inputBufferSize);
		} catch (LineUnavailableException ex) {
			System.out.println(getClass().getName()
					+ " : Error getting line\n");
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
	private boolean destroy() {
		sourceDataLine.drain();
		sourceDataLine.stop();
		sourceDataLine.close();
		sourceDataLine = null;
		if(mixer != null) {
			mixer.close();
			mixer = null;
		}
		return true;
	}

	/** Starts the audio system running. */
	@Override
	protected boolean start() {
		audioThread = new Thread(new Runnable() {
			public void run() {
				// create JavaSound stuff only when needed
				if(mixer == null) {
					selectMixer(0);
				}
				setupOutputJavaSound();
				if(hasInput) {
					setupInputJavaSound();
				}
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
		boolean isBigEndian = audioFormat.isBigEndian();
		int channels = audioFormat.getChannels();
		bbufOut = new byte[bufferSizeInFrames * audioFormat.getFrameSize()];
		sourceDataLine.start();
		if(hasInput) {
			bbufIn = new byte[bufferSizeInFrames * context.getInputAudioFormat().getFrameSize()];
			targetDataLine.start();
		}
		while (context.isRunning()) {
			if(hasInput && targetDataLine.available() > bbufIn.length) {
				targetDataLine.read(bbufIn, 0, bbufIn.length);
			} 
			boolean goodFrame = update(); // this propagates update call to context
			if (goodFrame) {
				if (isBigEndian) {
					for (int i = 0, counter = 0; i < bufferSizeInFrames; ++i) {
						for (int j = 0; j < channels; ++j) {
							short y = (short) (32767. * Math.min(Math.max(
									context.out.getValue(j, i), -1.0f), 1.0f));
							bbufOut[counter++] = (byte) ((y >> 8) & 0xFF);
							bbufOut[counter++] = (byte) (y & 0xFF);
						}
					}
				} else {
					for (int i = 0, counter = 0; i < bufferSizeInFrames; ++i) {
						for (int j = 0; j < channels; ++j) {
							short y = (short) (32767. * Math.min(Math.max(
									context.out.getValue(j, i), -1.0f), 1.0f));
							bbufOut[counter++] = (byte) (y & 0xFF);
							bbufOut[counter++] = (byte) ((y >> 8) & 0xFF);
						}
					}
				}
				sourceDataLine.write(bbufOut, 0, bbufOut.length);
			}
		}
	}
	
	@Override
	protected synchronized UGen getAudioInput(int[] channels) {
		if(targetDataLine == null) {
			if(context.isRunning()) {
				setupInputJavaSound();
				targetDataLine.start();
				bbufIn = new byte[context.getBufferSize() * context.getInputAudioFormat().getFrameSize()];
			} else {
				hasInput = true;
			}
		}
		return new JavaSoundRTInput(context, channels);
	}

	/**
	 * JavaSoundRTInput gathers audio from the JavaSound audio input device.
	 */
	private class JavaSoundRTInput extends UGen {

		private int[] channelsToServe;

		/**
		 * Instantiates a new RTInput.
		 * 
		 * @param context
		 *            the AudioContext.
		 * @param audioFormat
		 *            the AudioFormat.
		 */
		JavaSoundRTInput(AudioContext context, int[] channelsToServe) {
			super(context, channelsToServe.length);
			this.channelsToServe = channelsToServe;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.olliebown.beads.core.UGen#calculateBuffer()
		 */
		@Override
		public void calculateBuffer() {
			int ib;
			int offset = 0;
			if (context.getInputAudioFormat().isBigEndian()) {
				for (int i = 0; i < bufferSize; i++) {
					for (int j = 0; j < channelsToServe.length; j++) {
						ib = (channelsToServe[j] + offset) * 2;
						bufOut[j][i] = ((bbufIn[ib] << 8) | (bbufIn[ib + 1] & 0xFF)) / 32768.0F;
					}
					offset += context.getInputAudioFormat().getChannels();
				}
			} else {
				for (int i = 0; i < bufferSize; i++) {
					for (int j = 0; j < channelsToServe.length; j++) {
						ib = (channelsToServe[j] + offset) * 2;
						bufOut[j][i] = ((bbufIn[ib] & 0xFF) | (bbufIn[ib + 1] << 8)) / 32768.0F;
					}
					offset += context.getInputAudioFormat().getChannels();
				}
			}

		}

	}

}
