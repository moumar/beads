/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Takes an incoming signal (or signals in the multi-channel case) and adds
 * something (either a float value or another signal) to it (them).
 * 
 * @beads.category utilities
 * @author ollie
 * @author Benito Crawford
 * @version 0.9.5
 */
public class Add extends UGen {

	private UGen adderUGen;
	private float adder = 0;

	public Add(AudioContext context, int channels, UGen adderUGen) {
		super(context, channels, channels);
		setAdder(adderUGen);
	}

	public Add(AudioContext context, int channels, float adder) {
		super(context, channels, channels);
		setAdder(adder);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		if (adderUGen == null) {
			for (int j = 0; j < outs; j++) {
				float[] bi = bufIn[j];
				float[] bo = bufOut[j];
				for (int i = 0; i < bufferSize; i++) {
					bo[i] = bi[j] + adder;
				}
			}
		} else {
			adderUGen.update();
			if (outs == 1) {
				float[] bi = bufIn[0];
				float[] bo = bufOut[0];
				for (int i = 0; i < bufferSize; i++) {
					adder = adderUGen.getValue(0, i);
					bo[i] = bi[i] + adder;

				}
			} else {
				for (int i = 0; i < bufferSize; i++) {
					for (int j = 0; j < outs; j++) {
						adder = adderUGen.getValue(0, i);
						bufOut[j][i] = bufIn[j][i] + adder;
					}
				}
			}
		}
	}

	public float getAdder() {
		return adder;
	}

	public Add setAdder(float adder) {
		this.adder = adder;
		adderUGen = null;
		return this;
	}

	public Add setAdder(UGen adderUGen) {
		if (adderUGen == null) {
			setAdder(adder);
		} else {
			this.adderUGen = adderUGen;
			adderUGen.update();
			adder = adderUGen.getValue();
		}
		return this;
	}

	public UGen getAdderUGen() {
		return adderUGen;
	}

}
