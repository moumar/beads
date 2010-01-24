package net.beadsproject.beads.core;

/**
 * Organizes a series of connected UGens into one unit. It allows for users to
 * define a custom UGen purely from other UGens, without programming the
 * {@link UGen#calculateBuffer()} routine.
 * 
 * @author Benito Crawford
 * @version 0.9.5
 */
public abstract class Chain extends UGen {

	private UGen chainIn, chainOut;

	public Chain(AudioContext context, int ins, int outs) {
		super(context, ins, outs);

		// This grabs the inputs from this Chain instance, so they can be used
		// by UGens in the chain.
		chainIn = new UGen(context, 0, ins) {
			@Override
			public void calculateBuffer() {
			}
		};
		chainIn.bufOut = bufIn;
		chainIn.outputInitializationRegime = OutputInitializationRegime.RETAIN;

		// This collects the output of the chain and lets this Chain instance
		// grab the data.
		chainOut = new UGen(context, outs, 0) {
			@Override
			public void calculateBuffer() {
			}
		};
		this.bufOut = chainOut.bufIn;
		this.outputInitializationRegime = OutputInitializationRegime.RETAIN;

		defineChain();
	}

	/**
	 * The signal chain definition. Override this method to create a series of
	 * UGens which become the signal chain. The Chain object inputs can be added
	 * to chain UGens through {@link #addChainInputToUGen(int, UGen, int)
	 * addChainInputToUGen}; connect UGens to the Chain objects outputs with
	 * {@link #addChainOutput(int, UGen, int) addChainOutput}.
	 */
	public abstract void defineChain();

	/**
	 * Adds the Chain inputs to the target UGen's inputs.
	 * 
	 * @param targetUGen
	 *            The target UGen.
	 */
	protected void drawFromChainInput(UGen targetUGen) {
		targetUGen.addInput(chainIn);
	}

	/**
	 * Adds the specified Chain input to all of a target UGen's inputs.
	 * 
	 * @param chainInputIndex
	 *            The index of the Chain input.
	 * @param targetUGen
	 *            The UGen to which to add the Chain input.
	 */
	protected void drawFromChainInput(int chainInputIndex, UGen targetUGen) {
		for (int i = 0; i < targetUGen.ins; i++) {
			targetUGen.addInput(i, chainIn, chainInputIndex);
		}
	}

	/**
	 * 
	 * Adds the specified Chain input to a target UGen's input.
	 * 
	 * @param chainInputIndex
	 *            The index of the Chain input.
	 * @param targetUGen
	 *            The target UGen to which to add the Chain input.
	 * @param targetInputIndex
	 *            The input of the target UGen.
	 */
	protected void drawFromChainInput(int chainInputIndex, UGen targetUGen,
			int targetInputIndex) {
		targetUGen.addInput(targetInputIndex, chainIn, chainInputIndex);
	}

	/**
	 * Adds the output of a source UGen to the Chain output.
	 * 
	 * @param sourceUGen
	 *            The source UGen.
	 */
	protected void addToChainOutput(UGen sourceUGen) {
		chainOut.addInput(sourceUGen);
	}

	/**
	 * Adds all of the outputs of a source UGen to a Chain output.
	 * 
	 * @param chainOutputIndex
	 *            The Chain output.
	 * @param sourceUGen
	 *            The source UGen.
	 */
	protected void addToChainOutput(int chainOutputIndex, UGen sourceUGen) {
		for (int i = 0; i < sourceUGen.outs; i++) {
			addToChainOutput(chainOutputIndex, sourceUGen, i);
		}
	}

	/**
	 * Adds an output from a source UGen to a Chain output.
	 * 
	 * @param chainOutputIndex
	 *            The Chain output.
	 * @param sourceUGen
	 *            The source UGen.
	 * @param sourceOutputIndex
	 *            The output of the source UGen to add to the Chain output.
	 */
	protected void addToChainOutput(int chainOutputIndex, UGen sourceUGen,
			int sourceOutputIndex) {
		chainOut.addInput(chainOutputIndex, sourceUGen, sourceOutputIndex);
	}

	@Override
	public final void calculateBuffer() {
		chainOut.update();
	}
}
