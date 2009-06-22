/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Static;

/**
 * PanMonoToStereo pans a mono source between stereo channels.
 * 
 * @beads.category effect
 * @author ollie
 */
public class PanMonoToStereo extends UGen {

	/** The pan envelope. */
	private UGen panEnvelope;
	
	/**
	 * Instantiates a new PanMonoToStereo with default {@link Static} centre panned envelope.
	 * 
	 * @param context the AudioContext.
	 */
	public PanMonoToStereo(AudioContext context) {
		this(context, new Static(context, 0.5f));
	}
	
	/**
	 * Instantiates a new PanMonoToStereo.
	 * 
	 * @param context the AudioContext
	 * @param panEnvelope the pan envelope.
	 */
	public PanMonoToStereo(AudioContext context, UGen panEnvelope) {
		super(context, 1, 2);
		setPanEnvelope(panEnvelope);
	}

	/**
	 * Sets the pan envelope.
	 * 
	 * @param panEnvelope the new pan envelope.
	 */
	public void setPanEnvelope(UGen panEnvelope) {
		this.panEnvelope = panEnvelope;
	}
	
	/**
	 * Gets the pan envelope.
	 * 
	 * @return the pan envelope.
	 */
	public UGen getPanEnvelope() {
		return panEnvelope;
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			panEnvelope.update();
			float pan = panEnvelope.getValue(0, i);
			if(pan < 0) pan = 0;
			else if(pan > 1) pan = 1;
			bufOut[0][i] = bufIn[0][i] * pan;
			bufOut[1][i] = bufIn[0][i] * (1f - pan);
		}
	}
	
}
