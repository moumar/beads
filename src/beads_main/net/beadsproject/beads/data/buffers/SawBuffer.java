
package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SawBuffer.
 */
public class SawBuffer extends BufferFactory {

    public Buffer generateBuffer(int bufferSize) {
    	Buffer b = new Buffer(bufferSize);
        for(int i = 0; i < bufferSize; i++) {
            b.buf[i] = (float)i / (float)bufferSize * 2.0f - 1.0f;
        }
    	return b;
    }

    public String getName() {
    	return "Saw";
    }

    
}