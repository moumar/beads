package net.beadsproject.beads.data;

import java.io.Serializable;
import java.util.Hashtable;

// TODO: Auto-generated Javadoc
/**
 * A StaticBuffer stores a one-dimensional buffer of floats statically so that
 * it can be accessed by multiple classes without need for a reference to an
 * object. Users can request the buffer to be regenerated with different buffer
 * lengths. The assumption is that one buffer length will suffice across a whole
 * program.
 * 
 * @author ollie
 */
public class Buffer implements Serializable {
	
	public final static Hashtable<String, Buffer> staticBufs = new Hashtable<String, Buffer>();
	
	public final float[] buf;
	
	public Buffer(int size) {
		buf = new float[size];
	}
	
	/**
	 * Returns the value of the buffer at the given fraction along its length (0
	 * = start, 1 = end).
	 * 
	 * @param fraction
	 *            the point along the buffer to inspect.
	 * 
	 * @return the value at that point.
	 */
	public float getValueFraction(float fraction) {
		return getValueIndex((int)(fraction * buf.length));
	}

	/**
	 * Returns the value of the buffer at a specific index.
	 * 
	 * @param index
	 *            the index to inspect.
	 * 
	 * @return the value at that point.
	 */
	public float getValueIndex(int index) {
	       if(index < buf.length && index >= 0) 
	        	return buf[index];
	        else return 0.0f;
	}



}
