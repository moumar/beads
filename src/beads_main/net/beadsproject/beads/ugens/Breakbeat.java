/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;

/**
 * A {@link UGen} that plays back a {@link Sample} using {@link GranularSamplePlayer} such that the Sample's playback rate is controlled by an envelope that also controls the rate of a {@link Clock}. Breakbeat is actually a subclass of {@link Gain} and inherits its gain controls.
 *
 * @author ollie
 */
public class Breakbeat extends Gain {

	/** The GranularSamplePlayer. */
	private final GranularSamplePlayer gsp;
	
	/**
	 * Instantiates a new Breakbeat with the specified {@link AudioContext}, {@link Sample}, sample interval, and interval envelope.
	 * 
	 * @param context the AudioContext.
	 * @param s the Sample.
	 * @param sampleInterval a value, in milliseconds, that indicates the interval between beats in the sample.
	 * @param masterIntervalEnvelope a UGen which also controls a {@link Clock}. If the Breakbeat is triggered by the {@link Clock} then the Breakbeat and the {@link Clock} will be in synch.
	 */
	public Breakbeat(AudioContext context, Sample s, final float sampleInterval, UGen masterIntervalEnvelope) {
		super(context, s.nChannels);
		gsp = new GranularSamplePlayer(context, s);
		UGen rateCalculator = new UGen(context, 1, 1) {
			@Override
			public void calculateBuffer() {
				for(int i = 0; i < bufferSize; i++) {
					bufOut[0][i] =  sampleInterval/ bufIn[0][i];
				}
			}
		};
		rateCalculator.addInput(masterIntervalEnvelope);
		gsp.setRateEnvelope(rateCalculator);
		gsp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		gsp.getLoopStartEnvelope().setValue(0f);
		gsp.getLoopEndEnvelope().setValue(s.length);
		System.out.println(s.length);
		addInput(gsp);	
		setGainEnvelope(new Envelope(context, 1f));
	}
	
	/**
	 * Causes the GranularSamplePlayer to reset, and unpauses the Breakbeat if it is paused.
	 */
	public void reTrigger() {
		gsp.reTrigger();
		pause(false);
	}
	
	/**
	 * Gets the GranularSamplePlayer.
	 * 
	 * @return the GranularSamplePlayer.
	 */
	public GranularSamplePlayer getGSP() {
		return gsp;
	}

	/**
	 * Sets the position of the GranularSamplePlayer in milliseconds.
	 * 
	 * @param pos the new position.
	 */
	public void setPosition(float pos) {
		gsp.setPosition(pos);
	}

}
