/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import java.util.LinkedList;
import java.util.Queue;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * The Class PolyLimit is a mixer which can be used to keep a limit on the number of UGens connected to it.
 * An upper limit is given. If a new UGen is added but this takes the number of connected UGens over that 
 * upper limit then the oldest connected UGen is dropped in order to allow
 * the new UGen to be connected.
 * 
 * @beads.category utilities
 */
public class PolyLimit extends UGen {

	/** The max inputs. */
	private int maxInputs;
	
	/** The existing inputs. */
	private Queue<UGen> existingInputs;
	
	/**
	 * Instantiates a new PolyLimit.
	 * 
	 * @param context the context.
	 * @param inouts the number of channels.
	 * @param maxInputs the max number of connected inputs.
	 */
	public PolyLimit(AudioContext context, int inouts, int maxInputs) {
		super(context, inouts, inouts);
		setMaxInputs(maxInputs);
		existingInputs = new LinkedList<UGen>();
	}
	
	/**
	 * Overrides {@link UGen#addInput(UGen)} such that if a new UGen pushes the total number of 
	 * connected UGens above the upper limit, the oldest UGen is removed.
	 */
	public void addInput(UGen sourceUGen) {
		if(existingInputs.contains(sourceUGen)) {
			existingInputs.remove(sourceUGen);
			existingInputs.add(sourceUGen);
		} else {
			if(existingInputs.size() >= maxInputs) {
				UGen deadUGen = existingInputs.poll();
				removeAllConnections(deadUGen);			
			}
			existingInputs.add(sourceUGen);
			super.addInput(sourceUGen);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#removeAllConnections(net.beadsproject.beads.core.UGen)
	 */
	public void removeAllConnections(UGen sourceUGen) {
		super.removeAllConnections(sourceUGen);
		existingInputs.remove(sourceUGen);
	}

	/**
	 * Gets the max inputs.
	 * 
	 * @return the max inputs
	 */
	public int getMaxInputs() {
		return maxInputs;
	}

	/**
	 * Sets the max inputs.
	 * 
	 * @param maxInputs the new max inputs
	 */
	public void setMaxInputs(int maxInputs) {
		this.maxInputs = maxInputs;
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < ins; i++) {
			bufOut[i] = bufIn[i];	//TESTME
		}
	}

}
