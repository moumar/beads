/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.UGen;

public abstract class Function extends UGen {

	protected float[] x;
	private UGen[] inputs;
	
	public Function(UGen input) {
		this(new UGen[] {input});
	}
	
	public Function(UGen[] inputs) {
		super(inputs[0].getContext(), 1);
		this.inputs = inputs;
		x = new float[inputs.length];
	}
	
	public void calculateBuffer() {
		for(int i = 0; i < inputs.length; i++) {
			inputs[i].update();
		}
		for(int i = 0; i < bufferSize; i++) {
			for(int j = 0; j < inputs.length; j++) {
				x[j] = inputs[j].getValue(0, i);
			}
			bufOut[0][i] = calculate();
		}
	}
	
	/**
	 * Override this to calculate what to do.
	 * Use x[] to get the values from the input UGens.
	 * @return
	 */
	public abstract float calculate();

}
