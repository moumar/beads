package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;


public class CosineWindow extends BufferFactory {

	@Override
	public Buffer generateBuffer(int bufferSize) {
		Buffer b = new Buffer(bufferSize);
		for(int i = 0; i < bufferSize; i++) {
			b.buf[i] = (float)Math.sin((double)i / bufferSize * Math.PI);
		}
		return b;
	}

	@Override
	public String getName() {
		return "Cosine";
	}

}
