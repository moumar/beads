package net.beadsproject.beads.ugens;

import java.util.Arrays;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;


// TODO: Auto-generated Javadoc
/**
 * Adds two unit generators together. 
 */
public class Add extends UGen {

	/**
	 * Instantiates a new Add UGen with UGen a and UGen b added together.
	 * 
	 * @param context
	 *            the context
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public Add(AudioContext context, UGen a, UGen b) {
		this(context, Math.min(a.getOuts(), b.getOuts()));
		addInput(a);
		addInput(b);
	}
	
	public Add(AudioContext context, int inouts) {
		super(context, inouts, inouts);
//		bufOut = bufIn; 	//cheeky
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < outs; i++) {
			for(int j = 0; j < bufferSize; j++) {
				bufOut[i][j] = bufIn[i][j];
			}
		}
	}
	
	public static void main(String[] args) {
		float[] bufIn = new float[10];
		Arrays.fill(bufIn, 2f);
		float[] bufOut = bufIn;
		System.out.println(bufOut[5]);
	}

}
