
package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SineBuffer.
 */
public class SineBuffer extends BufferFactory {


    public Buffer generateBuffer(int bufferSize) {
    	Buffer b = new Buffer(bufferSize);
        for(int i = 0; i < bufferSize; i++) {
            b.buf[i] = (float)Math.sin(2.0 * Math.PI * (double)i / (double)bufferSize);
        }
    	return b;
    }

    public String getName() {
    	return "Sine";
    }

}
