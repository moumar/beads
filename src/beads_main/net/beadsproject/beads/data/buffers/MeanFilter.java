package net.beadsproject.beads.data.buffers;

import java.util.Arrays;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

/**
 * The convolution of the MeanFilter with data gives the mean.
 * 
 * @author ben
 *
 */
public class MeanFilter extends BufferFactory {

	@Override
	public Buffer generateBuffer(int bufferSize) {
		// TODO Auto-generated method stub
		Buffer b = new Buffer(bufferSize);
		Arrays.fill(b.buf,1.f/bufferSize);
		return b;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MeanFilter";
	}

}
