/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

public class Exp01Buffer extends BufferFactory {

	@Override
	public Buffer generateBuffer(int bufferSize) {
		Buffer b = new Buffer(bufferSize);
		for(int i = 0; i < bufferSize; i++) {
			float fract = (float)i / (float)(bufferSize - 1);
			b.buf[i] = (float)Math.exp(1f - 1f / fract);
		}
		return b;
	}

	@Override
	public String getName() {
		return "Exp01";
	}

}