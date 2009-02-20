
package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

// TODO: Auto-generated Javadoc
/**
 * A BufferFactory that gives you a Hanning Window.
 * 
 * @author ollie
 * @see Buffer
 */
public class HanningWindow extends BufferFactory {

    public Buffer generateBuffer(int bufferSize) {
    	Buffer b = new Buffer(bufferSize);
    	int lowerThresh = bufferSize / 4;
    	int upperThresh = bufferSize - lowerThresh;
    	for(int i = 0; i < bufferSize; i++) {
    		if(i < lowerThresh || i > upperThresh) {
    			b.buf[i] = 0.5f * (1.0f + (float)Math.cos((Math.PI + Math.PI * 4.0f * (float)i / (float)bufferSize)));
    		} else {
    			b.buf[i] = 1.0f;
    		}
    	}
    	return b;
    }
    
    public String getName() {
    	return "HanningWindow";
    }


    
}
