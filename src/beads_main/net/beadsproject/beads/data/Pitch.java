/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data;

/**
 * A set of static fields and utility methods associated with pitch.
 * 
 * @author ollie
 */
public abstract class Pitch {
	
	/** The constant log(2) = 0.6931472. */
	public final static float LOG2 = 0.6931472f;
    
	/**
	 * Convert frequency to MIDI note number.
	 * 
	 * @param frequency
	 *            the required frequency.
	 * 
	 * @return the resulting MIDI note number.
	 */
	public static final float ftom(float frequency) {
        return Math.max(0f, (float)Math.log(frequency / 440.0f) / LOG2 * 12f + 69f);
    }
    
	/**
	 * Convert MIDI note number to frequency.
	 * 
	 * @param midi
	 *            the required MIDI note number.
	 * 
	 * @return the resulting frequency.
	 */
	public static final float mtof(float midi) {
        return 440.0f * (float)Math.pow(2.0f, (midi - 69f) / 12.0f);
    }
	
	/** The dorian scale relative to root. */
	public static final int[] dorian = {0, 2, 3, 5, 7, 9, 10};

	/** The major scale relative to root. */
	public static final int[] major = {0, 2, 4, 5, 7, 9, 11};
	
	/** The minor scale relative to root. */
	public static final int[] minor = {0, 2, 3, 5, 7, 8, 10};
    
    /** The circle of fifths relative to root. */
    public static final int[] circleOfFifths = {0, 5, 10, 3, 8, 1, 6, 11, 4, 9, 2, 7};

}
