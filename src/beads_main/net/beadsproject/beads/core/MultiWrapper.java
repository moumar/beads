package net.beadsproject.beads.core;

import net.beadsproject.beads.data.*;

/**
 * A generalized multi-channel wrapper for UGens.
 * 
 * @author Benito Crawford
 * @version 0.9
 * 
 */
public abstract class MultiWrapper extends UGen implements DataBeadReceiver {
	private UGen[] ugens;
	private int channels, insPerChannel, outsPerChannel;
	private UGen mwIn, mwOut;

	/**
	 * Constructor for an multi-channel wrapper for 1-in/1-out UGens on each
	 * channel. {@link #buildUGens(int)} should be implemented to construct a
	 * UGen for each channel.
	 * 
	 * @param context
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 */
	public MultiWrapper(AudioContext context, int channels) {
		this(context, channels, 1, 1);
	}

	private MultiWrapper(AudioContext context, int numIns, int numOuts) {
		super(context, numIns, numOuts);

		/*
		 * This UGen facilitates getting signals from the inputs of the
		 * MultiWrap to the inputs of the channel UGens. Since we already did
		 * our adding in the pullInputs() method of the MultiWrap class, we'll
		 * just point to those input buffers with bufOut...
		 */
		mwIn = new UGen(context, 0, ins) {
			@Override
			public void calculateBuffer() {
			}
		};
		mwIn.bufOut = bufIn;
		mwIn.outputInitializationRegime = OutputInitializationRegime.RETAIN;

		// This collects the output of the chain and lets this MultiWrapper
		// instance
		// grab the data.
		mwOut = new UGen(context, outs, 0) {
			@Override
			public void calculateBuffer() {
			}
		};
		this.bufOut = mwOut.bufIn;
		this.outputInitializationRegime = OutputInitializationRegime.RETAIN;

	}

	/**
	 * Constructor for an n-channel wrapper for UGens with a certain number
	 * inputs and a certain number outputs on each channel.
	 * {@link #buildUGens(int)} should be implemented to construct a UGen for
	 * each channel.
	 * 
	 * @param context
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 * @param insPerChannel
	 *            The number of inputs per channel UGen.
	 * @param outsPerChannel
	 *            The number of outputs per channel UGen.
	 */
	public MultiWrapper(AudioContext context, int channels, int insPerChannel,
			int outsPerChannel) {
		this(context, channels * insPerChannel, channels * outsPerChannel);

		this.insPerChannel = insPerChannel;
		this.outsPerChannel = outsPerChannel;
		this.channels = channels;

		ugens = new UGen[channels];

		for (int i = 0; i < channels; i++) {
			// get our new UGen for channel i
			ugens[i] = buildUGens(i);
		}
		setupUGens();

	}

	/**
	 * Constructor for a multi-channel wrapper for an array of UGens that
	 * represent separate "channels".
	 * 
	 * @param context
	 *            The audio context.
	 * @param ugens
	 *            The array of UGens to wrap.
	 * @param insPerChannel
	 *            The number of inputs per channel.
	 * @param outsPerChannel
	 *            The number of ouputs per channel.
	 */
	public MultiWrapper(AudioContext context, UGen[] ugens, int insPerChannel,
			int outsPerChannel) throws Exception {

		this(context, ugens.length * insPerChannel, ugens.length
				* outsPerChannel);

		this.insPerChannel = insPerChannel;
		this.outsPerChannel = outsPerChannel;
		this.channels = ugens.length;
		this.ugens = ugens;

		setupUGens();

	}

	private void setupUGens() {

		for (int i = 0; i < channels; i++) {

			// hook the ins of the channel UGen to the appropriate output of
			// mwIn
			for (int j = 0; j < insPerChannel; j++) {
				if (j < ugens[i].ins) {
					ugens[i].addInput(j, mwIn, i * insPerChannel + j);
				}
			}

			// hook the outs of the channel UGen to the appropriate input
			// of mwOut
			for (int j = 0; j < outsPerChannel; j++) {
				if (j < ugens[i].outs) {
					mwOut.addInput(i * outsPerChannel + j, ugens[i], j);
				}
			}

		}
	}

	/**
	 * Constructs and returns a UGen for each channel. If an array of UGens is
	 * not provided to MCWrapper at construction, this method should be
	 * overridden with code to create a UGen for each channel. To make the
	 * channel UGens modifiable, the UGen should implement DataBeadReceiver
	 * properly as this is the only way to change the channel UGens' parameters.
	 * 
	 * @param channelIndex
	 *            The index of the channel for which the UGen is being created.
	 * @return The new channel UGen.
	 */
	public UGen buildUGens(int channelIndex) {
		return null;
	}

	@Override
	public void calculateBuffer() {
		mwOut.update();
	}

	/**
	 * A convenience method of adding inputs to specific channel UGens. Calling
	 * <p>
	 * <code>addInput(c, i, sUGen, o)</code> is equivalent to calling
	 * <p>
	 * <code>addInput(c * insPerChannel + i, sUGen, o)</code>.
	 * 
	 * @param channelIndex
	 *            The channel to call addInput on.
	 * @param channelUGenInput
	 *            The input index of the channel UGen.
	 * @param sourceUGen
	 *            The source UGen.
	 * @param sourceOutput
	 *            The output of the source UGen.
	 */
	public void addInput(int channelIndex, int channelUGenInput,
			UGen sourceUGen, int sourceOutput) {
		addInput(channelIndex * insPerChannel + channelUGenInput, sourceUGen,
				sourceOutput);
	}

	/**
	 * Forwards a DataBead to all channel UGens. If the channel UGens are
	 * DataBeadReceivers, it calls {@link DataBeadReceiver#sendData(DataBead)};
	 * otherwise it calls {@link Bead#message(Bead)}.
	 * 
	 * @param db
	 *            The DataBead.
	 */
	public DataBeadReceiver sendData(DataBead db) {
		for (int i = 0; i < channels; i++) {
			if (ugens[i] instanceof DataBeadReceiver) {
				((DataBeadReceiver) ugens[i]).sendData(db);
			} else {
				ugens[i].message(db);
			}
		}
		return this;
	}

	/**
	 * Forwards a DataBead to a specific channel UGen. If the channel UGen is a
	 * DataBeadReceiver, it calls {@link DataBeadReceiver#sendData(DataBead)};
	 * otherwise it calls {@link Bead#message(Bead)}.
	 * 
	 * @param channel
	 *            The index of the channel UGen to which to forward the
	 *            DataBead.
	 * @param db
	 *            The DataBead.
	 */
	public DataBeadReceiver sendData(int channel, DataBead db) {
		for (int i = 0; i < channels; i++) {
			if (ugens[i] instanceof DataBeadReceiver) {
				((DataBeadReceiver) ugens[i]).sendData(db);
			} else {
				ugens[i].message(db);
			}
		}
		return this;
	}

	/**
	 * Forwards Beads to channel UGens. If <code>message</code> is a DataBead
	 * and the channel UGens are DataBeadReceivers, it calls
	 * {@link DataBeadReceiver#sendData(DataBead)}; otherwise it calls
	 * {@link Bead#message(Bead)}.
	 */
	public void messageReceived(Bead message) {
		if (message instanceof DataBead) {
			sendData((DataBead) message);
		} else {
			for (int i = 0; i < channels; i++) {
				ugens[i].message(message);

			}
		}
	}

	/**
	 * Gets the number of channels.
	 * 
	 * @return The number of channels.
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * Gets the number of inputs per channel.
	 * 
	 * @return The number of inputs per channel.
	 */
	public int getInsPerChannel() {
		return insPerChannel;
	}

	/**
	 * Gets the number of outs per channel.
	 * 
	 * @return The number of outs per channel.
	 */
	public int getOutsPerChannel() {
		return outsPerChannel;
	}

}
