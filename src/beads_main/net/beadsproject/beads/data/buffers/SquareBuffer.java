/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

/**
 * A {@link BufferFactory} that generates square waves.
 * 
 * @see Buffer BufferFactory
 * @author ollie
 */
public class SquareBuffer extends BufferFactory {

    /* (non-Javadoc)
     * @see net.beadsproject.beads.data.BufferFactory#generateBuffer(int)
     */
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

    /* (non-Javadoc)
     * @see net.beadsproject.beads.data.BufferFactory#getName()
     */
    public String getName() {
    	return "Square";
    }

    
}