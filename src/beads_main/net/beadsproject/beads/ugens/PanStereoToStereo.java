/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * PanStereoToStereo pans a stereo source between two stereo channels.
 * 
 * @beads.category effect
 * @author ollie
 */
public class PanStereoToStereo extends UGen {

	/** The pan envelope. */
	private UGen panEnvelope;

	/**
	 * Instantiates a new PanStereoToStereo.
	 * 
	 * @param context the AudioContext.
	 */
	public PanStereoToStereo(AudioContext context) {
		super(context, 2, 2);
		panEnvelope = new Static(context, 0.5f);
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		panEnvelope.update();
		for(int i = 0; i < bufferSize; i++) {
			float pan = Math.max(0, Math.min(1, panEnvelope.getValue(0, i)));
			if(pan < 0.5f) {
				bufOut[0][i] = 2f * bufIn[0][i] * pan;
				bufOut[1][i] = bufIn[1][i];	
			} else {
				bufOut[0][i] = bufIn[0][i];
				bufOut[1][i] = 2f * bufIn[1][i] * (1f - pan);	
			}
			//TODO this is a lazy panning algorithm.
		}
	}

	/**
	 * Gets the pan envelope.
	 * 
	 * @return the pan envelope.
	 */
	public UGen getPanEnvelope() {
		return panEnvelope;
	}

	/**
	 * Sets the pan envelope.
	 * 
	 * @param panEnvelope the new pan envelope.
	 */
	public void setPanEnvelope(UGen panEnvelope) {
		this.panEnvelope = panEnvelope;
	}
	
}
