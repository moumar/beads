
package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SawBuffer.
 */
public class SquareBuffer extends BufferFactory {

    public Buffer generateBuffer(int bufferSize) {
    	Buffer b = new Buffer(bufferSize);
    	int halfBufferSize = bufferSize / 2;
        for(int i = 0; i < halfBufferSize; i++) {
            b.buf[i] = 1f;
        }
        for(int i = halfBufferSize; i < bufferSize; i++) {
            b.buf[i] = -1f;
        }
    	return b;
    }

    public String getName() {
    	return "Square";
    }

    
}