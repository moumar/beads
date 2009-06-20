package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

public class Curve extends BufferFactory {

	private final float curviness;
	
	public Curve(float curviness) {
		this.curviness = Math.min(1, Math.max(-1, curviness));
	}
	
	@Override
	public Buffer generateBuffer(int bufferSize) {
		Buffer b = new Buffer(bufferSize);
		double exponent = Math.exp(-curviness);
		for(int i = 0; i < bufferSize; i++) {
			b.buf[i] = (float)Math.pow((float)i / bufferSize, exponent);
		}
		return b;
	}

	@Override
	public String getName() {
		return "Curve " + curviness;
	}

}
