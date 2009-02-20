package net.beadsproject.beads.ugens;

import java.util.ArrayList;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;


// TODO: Auto-generated Javadoc
/**
 * The Class DelayLine.
 */
public class DelayLine extends UGen {

	//INCOMPLETE
	
	/** The buffs. */
	private ArrayList<Float>[] buffs;
	
	/** The index. */
	private int index = 0;
	
	/** The max. */
	private int max = 100;
	
	/**
	 * Instantiates a new delay line.
	 * 
	 * @param context
	 *            the context
	 * @param inouts
	 *            the inouts
	 * @param msLength
	 *            the ms length
	 */
	public DelayLine(AudioContext context, int inouts, float msLength) {
		super(context, inouts, inouts);
		buffs = new ArrayList[inouts];
		for(int i = 0; i < inouts; i++) {
			buffs[i] = new ArrayList<Float>();
		}
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			for(int j = 0; j < ins; j++) {
				buffs[j].set(index, bufIn[j][i]);
			}
			index = (index + 1) % max;
		}
	}

}
