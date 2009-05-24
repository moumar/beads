package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

public class Log01Buffer extends BufferFactory {

	@Override
	public Buffer generateBuffer(int bufferSize) {
		Buffer b = new Buffer(bufferSize);
		for(int i = 0; i < bufferSize; i++) {
			float fract = (float)i / (float)(bufferSize - 1);
			b.buf[i] = 1f / (1f - (float)Math.log(fract));
		}
		return b;
	}

	@Override
	public String getName() {
		return "Log01";
	}

}
