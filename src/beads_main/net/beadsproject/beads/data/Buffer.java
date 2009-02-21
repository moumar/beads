/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * A Buffer stores a one-dimensional buffer of floats for use as a wavetable or a window.
 * 
 * @see Sample BufferFactory
 * @author ollie
 */
public class Buffer implements Serializable {
	
	//TODO add linear and cubic interpolation
	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * A static storage area for common buffers, such as a sine wave. Used by {@link BufferFactory} to keep track of common buffers.
	 */
	public final static Hashtable<String, Buffer> staticBufs = new Hashtable<String, Buffer>();
	
	/** 
	 * The buffer data. 
	 */
	public final float[] buf;
	
	/**
	 * Instantiates a new buffer.
	 * 
	 * @param size the size of the buffer.
	 */
	public Buffer(int size) {
		buf = new float[size];
	}
	
	/**
	 * Returns the value of the buffer at the given fraction along its length (0 = start, 1 = end).
	 * 
	 * @param fraction the point along the buffer to inspect.
	 * 
	 * @return the value at that point.
	 */
	public float getValueFraction(float fraction) {
		return getValueIndex((int)(fraction * buf.length));
	}

	/**
	 * Returns the value of the buffer at a specific index.
	 * 
	 * @param index the index to inspect.
	 * 
	 * @return the value at that point.
	 */
	public float getValueIndex(int index) {
	       if(index < buf.length && index >= 0) 
	        	return buf[index];
	        else return 0.0f;
	}



}
