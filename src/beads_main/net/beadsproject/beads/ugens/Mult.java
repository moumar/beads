/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Takes an incoming signal (or signals in the multi-channel case) and
 * multiplies it with something (another signal or a float value).
 * 
 * @beads.category utilities
 * @author ollie
 * @author Benito Crawford
 */
public class Mult extends UGen {

	private float multiplier;
	private UGen multiplierUGen;

	public Mult(AudioContext context, int channels, float multiplier) {
		super(context, channels, channels);
		setMultiplier(multiplier);
	}

	public Mult(AudioContext context, int channels, UGen multiplierUGen) {
		super(context, channels, channels);
		setMultiplier(multiplierUGen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		if (multiplierUGen == null) {
			for (int j = 0; j < outs; j++) {
				float[] bi = bufIn[j];
				float[] bo = bufOut[j];

				for (int i = 0; i < bufferSize; i++) {
					bo[i] = bi[i] * multiplier;
				}
			}
		} else {
			multiplierUGen.update();
			if (outs == 1) {
				float[] bi = bufIn[0];
				float[] bo = bufOut[0];
				for (int i = 0; i < bufferSize; i++) {
					multiplier = multiplierUGen.getValue(0, i);
					bo[i] = bi[i] * multiplier;

				}
			} else {
				for (int i = 0; i < bufferSize; i++) {
					for (int j = 0; j < outs; j++) {
						multiplier = multiplierUGen.getValue(0, i);
						bufOut[j][i] = bufIn[j][i] * multiplier;
					}
				}
			}
		}
	}

	public float getMultiplier() {
		return multiplier;
	}

	public Mult setMultiplier(float multiplier) {
		this.multiplier = multiplier;
		multiplierUGen = null;
		return this;
	}

	public Mult setMultiplier(UGen multiplierUGen) {
		if (multiplierUGen == null) {
			setMultiplier(multiplier);
		} else {
			this.multiplierUGen = multiplierUGen;
			multiplierUGen.update();
			multiplier = multiplierUGen.getValue();
		}
		return this;
	}

	public UGen getMultiplierUGen() {
		return multiplierUGen;
	}

}
