package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

/**
 * Filter to be used for smoothing data (see OnsetDetector)
 * 
 * @author ben
 *
 */
public class RampBuffer extends BufferFactory {

	@Override
	public Buffer generateBuffer(int bufferSize) {
		Buffer b = new Buffer(bufferSize);
		
		for (int i=0;i<bufferSize;i++)
		{
			b.buf[i] = ramp((i+0.5f)/bufferSize)/bufferSize; 
		}
		return b;
	}
	
	protected float ramp(float x)
	{
		return 2*x;	
	}

	@Override
	public String getName() {
		return "Ramp";
	}

}
