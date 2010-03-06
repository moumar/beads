package net.beadsproject.beads.core;


/**
 * A simple UGen that just forwards its inputs to its outputs.
 * 
 * @author Benito Crawford
 * @version 0.9.5
 * 
 */
public class Throughput extends UGen {

	/**
	 * Constructor for a one-channel Throughput using the specified audio
	 * context.
	 * 
	 * @param context
	 *            The audio context.
	 */
	public Throughput(AudioContext context) {
		this(context, 1);
	}

	/**
	 * Constructor for a Throughput with the specified number of channels, using
	 * the specified audio context.
	 * 
	 * @param context
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 */
	public Throughput(AudioContext context, int channels) {
		super(context, channels, channels);
		this.outputInitializationRegime = OutputInitializationRegime.RETAIN;
		bufOut = bufIn;
	}

	@Override
	public void calculateBuffer() {
	}

}
