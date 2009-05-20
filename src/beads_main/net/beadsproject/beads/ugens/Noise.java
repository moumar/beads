/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.NoiseBuffer;

/**
 * Noise generates white noise.
 * 
 * @author ollie
 */
public class Noise extends UGen {

	private Buffer noiseBuffer;
	private int index;
	
	/**
	 * Instantiates a new Noise.
	 * 
	 * @param context the AudioContext.
	 */
	public Noise(AudioContext context) {
		super(context, 1);
		if(!Buffer.staticBufs.containsKey("noise")) {
			noiseBuffer = new NoiseBuffer().generateBuffer(200000);
        	Buffer.staticBufs.put("noise", noiseBuffer);
    	} else {
    		noiseBuffer = Buffer.staticBufs.get("noise");
    	}
		index = 0;
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			bufOut[0][i] = noiseBuffer.getValueIndex(index);
			index++;
			if(index == noiseBuffer.buf.length) {
				index = 0;
			}
		}
	}
	
	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		ac.out.addInput(new Noise(ac));
		ac.start();
	}

}
