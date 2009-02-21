/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;
import java.util.Arrays;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Static;

/**
 * A basic Filter thingamyjig.
 * 
 * @author ollie
 */
public class Filter extends UGen {

	/** The alpha envelope. */
	private UGen alphaEnvelope;
	
	/** The previous value. */
	private float[] lastValue;

	/**
	 * Instantiates a new Filter.
	 * 
	 * @param context the AudioContext.
	 * @param inouts the number of ins (= number of outs).
	 */
	public Filter(AudioContext context, int inouts) {
		this(context, inouts, 0.5f);
	}
	

	/**
	 * Instantiates a new Filter.
	 * 
	 * @param context the AudioContext.
	 * @param inouts the the number of ins (= number of outs).
	 * @param alpha the alpha value.
	 */
	public Filter(AudioContext context, int inouts, float alpha) {
		this(context, inouts, new Static(context, alpha));
	}

	/**
	 * Instantiates a new Filter.
	 * 
	 * @param context the AudioContext.
	 * @param inouts the the number of ins (= number of outs).
	 * @param alphaEnvelope the alpha envelope.
	 */
	public Filter(AudioContext context, int inouts, UGen alphaEnvelope) {
		super(context, inouts, inouts);
		lastValue = new float[inouts];
		Arrays.fill(lastValue, 0f);
		setAlphaEnvelope(alphaEnvelope);
	}

	/**
	 * Sets the alpha envelope.
	 * 
	 * @param alphaEnvelope the new alpha envelope.
	 */
	public void setAlphaEnvelope(UGen alphaEnvelope) {
		this.alphaEnvelope = alphaEnvelope;
	}

	/**
	 * Gets the alpha envelope.
	 * 
	 * @return the alpha envelope
	 */
	public UGen getAlphaEnvelope() {
		return alphaEnvelope;
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		alphaEnvelope.update();
		for(int i = 0; i < ins; i++) {
			for(int j = 0; j < bufferSize; j++) {
				bufOut[i][j] = lastValue[i] + alphaEnvelope.getValue(0, j) * (bufIn[i][j] - lastValue[i]);
				lastValue[i] = bufOut[i][j];
			}
		}
	}


}