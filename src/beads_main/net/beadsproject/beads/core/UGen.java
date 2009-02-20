package net.beadsproject.beads.core;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: Auto-generated Javadoc
/**
 * A UGen is the main class for signal generation and processing. It inherits
 * start(), kill() and pause() methods and the self-deleting behaviour from
 * Bead, as well as the messaging system. UGens are constructed using an
 * AudioContext to determine the buffer size and have to be specified with a
 * given number of inputs and outputs. By connecting a UGen's output to another
 * UGen's input the UGen is automatically added to a call chain that propagates
 * through subsequent UGens from the root UGen of the AudioContext. UGens that
 * do not have outputs (such as Clocks and FrameFeatureExtractors) can be added
 * manually to the call chain using {@link #addDependent(UGen)} from any UGen
 * that is part of the call chain (such as the root of the AudioContext).</p>
 * 
 * When this call chain is propagated, each UGen checks to make sure that its
 * input UGens are not deleted. It deletes references to any that are. Since
 * Beads are set by default to self-delete, killing a UGen will therefore cause
 * it to get dropped from the call chain.
 * 
 * @author ollie
 */
public abstract class UGen extends Bead {

	/** The AudioContext used by this buffer. */
	protected AudioContext context;
	
	/** The number of inputs. */
	protected int ins;
	
	/** The number of outputs. */
	protected int outs;
	
	/** The buffer used internally to store input data. */
	protected float[][] bufIn;
	
	/** The buffer that will be grabbed by others. */
	protected float[][] bufOut;
	
	/** The buffer size. This is set to be the AudioContext bufferSize when the AudioContext is set.*/
	protected int bufferSize;
	
	/** Pointers to the input buffers. */
	private ArrayList<BufferPointer>[] inputs;
	
	/** Other UGens that should be triggered by this one. */
	private ArrayList<UGen> dependents;
	
	/** Used to avoid calling pullInputs unless required. */
	private boolean noInputs;
	
	/** Keep track of whether we've updated at this timeStep. */
	private int lastTimeStep;

	/**
	 * Create a new UGen from the given AudioContext but with no inputs or
	 * outputs.
	 * 
	 * @param context
	 *            AudioContext to use.
	 */
	public UGen(AudioContext context) {
		this(context, 0, 0);
	}
	
	/**
	 * Create a new UGen from the given AudioContext with no inputs and the
	 * given number of outputs.
	 * 
	 * @param context
	 *            AudioContext to use.
	 * @param outs
	 *            number of outputs.
	 */
	public UGen(AudioContext context, int outs) {
		this(context, 0, outs);
	}

	/**
	 * Create a new UGen from the given AudioContext with the given number of
	 * inputs and outputs.
	 * 
	 * @param context
	 *            AudioContext to use.
	 * @param ins
	 *            number of inputs.
	 * @param outs
	 *            number of outputs.
	 */
	public UGen(AudioContext context, int ins, int outs) {
		dependents = new ArrayList<UGen>();
		noInputs = true;
		lastTimeStep = -1;
		setIns(ins);
		setOuts(outs);
		setContext(context);
	}
	
	
	/**
	 * Sets the AudioContext used by this UGen. Resetting the AudioContext after initialization could have unexpected consequences.
	 * 
	 * @param context
	 */
	private void setContext(AudioContext context) {
		this.context = context;
		if(context != null) {
			bufferSize = context.getBufferSize();
			setupInputBuffer();
			setupOutputBuffer();
		} else {
			bufIn = null;
			bufOut = null;
		}
	}

	/**
	 * Gets the AudioContext used by this UGen.
	 * 
	 * @return the AudioContext
	 */
	public AudioContext getContext() {
		return context;
	}

	/**
	 * Set the number of inputs.
	 * 
	 * @param ins
	 *            number of inputs.
	 */
	private void setIns(int ins) {
		this.ins = ins;
		inputs = new ArrayList[ins];
		for (int i = 0; i < ins; i++) {
			inputs[i] = new ArrayList<BufferPointer>();
		}
	}
	
	/**
	 * Gets the number of inputs.
	 * 
	 * @return number of inputs.
	 */
	public int getIns() {
		return ins;
	}

	/**
	 * Sets the number of outputs.
	 * 
	 * @param outs
	 *            number of outputs.
	 */
	private void setOuts(int outs) {
		this.outs = outs;
	}

	/**
	 * Gets the number of outputs.
	 * 
	 * @return number of outputs.
	 */
	public int getOuts() {
		return outs;
	}
	
	private void setupInputBuffer() {
		bufIn = new float[ins][bufferSize];
		zeroIns();
	}
	
	private void setupOutputBuffer() {
		bufOut = new float[outs][bufferSize];
		zeroOuts();
	}
	
	/**
	 * Sets the output buffers to zero.
	 */
	public void zeroOuts() {
		for(int i = 0; i < outs; i++) {
			Arrays.fill(bufOut[i], 0f);
		}
	}

	/**
	 * Set the input buffers to zero.
	 */
	public void zeroIns() {
		for(int i = 0; i < ins; i++) {
			Arrays.fill(bufIn[i], 0f);
		}
	}
	
	/**
	 * Pull inputs.
	 */
	private synchronized void pullInputs() {
		ArrayList<UGen> dependentsClone = (ArrayList<UGen>) dependents.clone();
		int size = dependentsClone.size();
		for (int i = 0; i < size; i++) {
			UGen dependent = dependentsClone.get(i);
			if (dependent.isDeleted())
				removeDependent(dependent);
			else
				dependent.update();
		}
		if (!noInputs) {
			for (int i = 0; i < inputs.length; i++) {
				ArrayList<BufferPointer> inputsCopy = (ArrayList<BufferPointer>) inputs[i].clone();
				size = inputsCopy.size();
					Arrays.fill(bufIn[i], 0f);
					for (int ip = 0; ip < size; ip++) {
						BufferPointer bp = inputsCopy.get(ip);
						if (bp.ugen.isDeleted())
							inputs[i].remove(bp);
						else {
							bp.ugen.update();
							for (int j = 0; j < bufferSize; j++) {
								bufIn[i][j] += bp.get(j);
							}
						}
					}
			}
		}
	}

	/**
	 * Updates the UGen. If the UGen is muted or has already been updated at
	 * this time step (according to the AudioContext) then this method does nothing. If the UGen does update, it
	 * will also call the update() method on all UGens connected to its inputs or depending on it.
	 */
	public synchronized void update() {
		if (!isUpdated() && !isPaused()) {
			lastTimeStep = context.getTimeStep(); // do this first to break call
			// chain loops
			pullInputs();
			calculateBuffer();
		}
	}

	/**
	 * Prints the UGens connected to this UGen's inputs to the Standard Output.
	 */
	public synchronized void printInputList() {
		for (int i = 0; i < inputs.length; i++) {
			for (BufferPointer bp : inputs[i]) {
				System.out.print(bp.ugen + " ");
			}
		}
		System.out.println();
	}

	/**
	 * Maximally connect another UGen to the inputs of this UGen. If the number
	 * of outputs is greater than the number of inputs then the extra outputs are not connected. If the number of inputs is greater than the number of outputs then the outputs are cycled to fill all inputs. If
	 * multiple UGens are connected to any one input then the outputs from those
	 * UGens are summed on their way into the input.
	 * 
	 * @param sourceUGen
	 *            the UGen to connect to this UGen.
	 */
	public synchronized void addInput(UGen sourceUGen) {
		if(ins != 0 && sourceUGen.outs != 0) {
			for (int i = 0; i < ins; i++) {
				addInput(i, sourceUGen, i % sourceUGen.outs);
			}
		}
	}

	/**
	 * Connect a specific output from another UGen to a specific input of this
	 * UGen.
	 * 
	 * @param inputIndex
	 *            the input of this UGen to connect to.
	 * @param sourceUGen
	 *            the UGen to connect to this UGen.
	 * @param sourceOutputIndex
	 *            the output of the connecting UGen with which to make the
	 *            connection.
	 */
	public synchronized void addInput(int inputIndex, UGen sourceUGen,
			int sourceOutputIndex) {
		inputs[inputIndex]
				.add(new BufferPointer(sourceUGen, sourceOutputIndex));
		noInputs = false;
	}

	/**
	 * Adds a UGen to this UGen's dependency list, causing the dependent UGen to
	 * get updated when this one does.
	 * 
	 * @param dependent
	 *            the dependent UGen.
	 */
	public void addDependent(UGen dependent) {
		dependents.add(dependent);
	}

	/**
	 * Removes the specified UGen from this UGen's dependency list.
	 * 
	 * @param dependent
	 *            UGen to remove.
	 */
	public void removeDependent(UGen dependent) {
		dependents.remove(dependent);
	}

	/**
	 * Gets the number of UGen outputs connected at the specified input index of
	 * this UGen.
	 * 
	 * @param index
	 *            index of input to inspect.
	 * 
	 * @return number of UGen outputs connected to that input.
	 */
	public int getNumberOfConnectedUGens(int index) {
		return inputs[index].size();
	}

	/**
	 * Disconnects the specified UGen from this UGen at all inputs.
	 * 
	 * @param sourceUGen
	 *            the UGen to disconnect.
	 */
	public void removeAllConnections(UGen sourceUGen) {
		if (!noInputs) {
			int inputCount = 0;
			for (int i = 0; i < inputs.length; i++) {
				ArrayList<BufferPointer> bplist = (ArrayList<BufferPointer>) inputs[i].clone();
				for (BufferPointer bp : bplist) {
					if (sourceUGen.equals(bp.ugen)) {
						inputs[i].remove(bp);
					} else
						inputCount++;
				}
			}
			if (inputCount == 0) {
				noInputs = true;
				zeroIns();
			}
		}
	}
	
	public void clearInputConnections() {
		for(int i = 0; i < inputs.length; i++) {
			ArrayList<BufferPointer> bplist = (ArrayList<BufferPointer>) inputs[i].clone();
			for (BufferPointer bp : bplist) {
				inputs[i].remove(bp);
			}
			noInputs = true;
			zeroIns();
		}
	}

	/**
	 * Prints the contents of the output buffers to the Standard Input. Could be
	 * a lot of data.
	 */
	public void printOutBuffers() {
		for (int i = 0; i < bufOut.length; i++) {
			for (int j = 0; j < bufOut[i].length; j++) {
				System.out.print(bufOut[i][j] + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Determines whether this UGen has no UGens connected to its inputs.
	 * 
	 * @return true if this UGen has no UGens connected to its inputs, false
	 *         otherwise.
	 */
	public boolean noInputs() {
		return noInputs;
	}

	/**
	 * Called by the signal chain to update this UGen's ouput data. Subclassses of UGen should put the UGen's DSP perform routine in here. In
	 * general this involves grabbing data from {@link #bufIn} and putting data
	 * into {@link #bufOut} in some way. {@link #bufIn} and {@link #bufOut} are 2D arrays of floats of the form float[numChannels][bufferSize]. The length of the buffers is given by
	 * {@link #bufferSize}, and the number of channels of the input and output buffers are given by {@link #ins} and {@link #outs} respectively.
	 */
	public abstract void calculateBuffer(); // must be implemented by subclasses

	/**
	 * Gets a specific specified value from the output buffer, with indices i (channel)
	 * and j (offset into buffer).
	 * 
	 * @param i
	 *            channel index.
	 * @param j
	 *            buffer frame index.
	 * 
	 * @return value of specified sample.
	 */
	public float getValue(int i, int j) {
		return bufOut[i][j];
	}
	
	/**
	 * Gets the value of the buffer, assuming that the buffer only has one value. This is mainly a convenience method for use with {@link #Static} type UGens.
	 * 
	 * @return the value
	 */
	public float getValue() {
		return bufOut[0][0];
	}
	
	/**
	 * Sets the value of the buffer, assuming that the buffer only has one value. This is mainly a convenience method for use with {@link #Static} and {@link #Envelope} type UGens.
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(float value) {
	}

	/**
	 * Checks if is updated.
	 * 
	 * @return true, if is updated
	 */
	private boolean isUpdated() {
		return lastTimeStep == context.getTimeStep();
	}
	
	public void pause(boolean paused) {
		if(!isPaused() && paused) {
			zeroOuts();
		}
		super.pause(paused);
	}

	/**
	 * The Class BufferPointer.
	 */
	private class BufferPointer {

		/** The ugen. */
		UGen ugen;
		
		/** The index. */
		int index;

		/**
		 * Instantiates a new buffer pointer.
		 * 
		 * @param ugen
		 *            the ugen to point to.
		 * @param index
		 *            the index of the output of that ugen.
		 */
		BufferPointer(UGen ugen, int index) {
			this.ugen = ugen;
			this.index = index;
		}

		/**
		 * Gets the buffer.
		 * 
		 * @return the buffer
		 */
		float[] getBuffer() {
			return ugen.bufOut[index];
		}
		
		/**
		 * Gets the value at the given time point.
		 * 
		 * @param point
		 *            the point
		 * 
		 * @return the float
		 */
		float get(int point) {
			return ugen.getValue(index, point);
		}
	
	}

}
