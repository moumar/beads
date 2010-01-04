package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;

/**
 * A simple panning object that takes a mono input and gives stereo output.
 * Power is kept constant regardless of position; note that center-panning a
 * signal will yield the input signal multiplied by 1 / sqrt(2) in each output
 * channel as a result. A pan value of -1 pans completely to the left, 1 pans
 * completely to the right, and 0 results in center panning. It uses an array to
 * approximate square roots for efficiency.
 * 
 * @author Benito Crawford
 * @version 0.9.1
 */
public class Panner extends UGen {

	protected static int rootSize = 1024;
	public static float[] ROOTS = buildRoots(rootSize);
	private int currsample = 0;
	protected float pos = 0, p1, p2;
	protected UGen posUGen;
	protected ParamUpdater pu;

	/**
	 * Constructor that sets the pan to the middle by default.
	 * 
	 * @param con
	 *            The audio context.
	 */
	public Panner(AudioContext con) {
		this(con, 0);
	}

	/**
	 * Constructor that sets the pan to a static value.
	 * 
	 * @param con
	 *            The audio context.
	 * @param ipos
	 *            The initial pan value.
	 */
	public Panner(AudioContext con, float ipos) {
		super(con, 1, 2);
		pos = ipos;
		constructPU();
	}

	/**
	 * Constructor that sets a UGen to specify the pan value.
	 * 
	 * @param con
	 *            The audio context.
	 * @param posUGen
	 *            The pan UGen.
	 */
	public Panner(AudioContext con, UGen posUGen) {
		super(con, 1, 2);
		this.posUGen = posUGen;
		constructPU();
	}

	/**
	 * To set up the ParamUpdater.
	 */
	protected void constructPU() {
		if (posUGen == null) {
			if (pu == null || pu.type != 0) {
				pu = new ParamUpdater(0);
			}
			calcVals();
		} else {
			if (pu == null || pu.type != 1) {
				pu = new ParamUpdater(1) {
					void updateUGens() {
						posUGen.update();
					}

					void updateParams() {
						pos = posUGen.getValue(0, currsample);
						calcVals();
					}
				};
			}
		}

	}

	@Override
	public void calculateBuffer() {

		float[] bi = bufIn[0];
		float[] bo1 = bufOut[0];
		float[] bo2 = bufOut[1];

		pu.updateUGens();

		for (int currsample = 0; currsample < bufferSize; currsample++) {
			pu.updateParams();
			bo1[currsample] = p1 * bi[currsample];
			bo2[currsample] = p2 * bi[currsample];
		}
	}

	protected void calcVals() {
		if (pos >= 1) {
			p1 = 0;
			p2 = 1;
		} else if (pos <= -1) {
			p1 = 1;
			p2 = 0;
		} else {
			int n1;
			float f = (pos + 1) * .5f * (float) rootSize;
			f -= (n1 = (int) Math.floor(f));
			p2 = ROOTS[n1] * (1 - f) + ROOTS[n1 + 1] * f;
			p1 = ROOTS[rootSize - n1] * (1 - f) + ROOTS[rootSize - (n1 + 1)]
					* f;
		}
	}
	
	/**
	 * Calculates an array of square-roots from 0 to 1.
	 * @param rs	The size of the array minus 2.
	 * @return		The array.
	 */
	protected static float[] buildRoots(int rs) {
		float[] roots = new float[rs + 2];
		for (int i = 0; i < rs + 1; i++) {
			roots[i] = (float) Math.sqrt((float) i / rs);
		}
		roots[rs + 1] = 1;
		return roots;
	}

	/**
	 * For efficiency.
	 * 
	 * @author Benito Crawford
	 * 
	 */
	protected class ParamUpdater {
		int type;

		ParamUpdater(int type) {
			this.type = type;
		}

		void updateUGens() {
		}

		void updateParams() {
		}
	}

	/**
	 * Gets the current pan position.
	 * 
	 * @return The pan position.
	 */
	public float getPos() {
		return pos;
	}

	/**
	 * Sets the pan position to a static float value.
	 * 
	 * @param pos
	 *            The pan position.
	 */
	public void setPos(float pos) {
		this.pos = pos;
		posUGen = null;
		constructPU();
	}

	/**
	 * Sets a UGen to specify the pan position.
	 * 
	 * @param posUGen
	 *            The pan UGen.
	 */
	public void setPos(UGen posUGen) {
		this.posUGen = posUGen;
		constructPU();
	}

	/**
	 * Gets the pan UGen, if it exists.
	 * 
	 * @return The pan UGen.
	 */
	public UGen getPosUGen() {
		return posUGen;
	}

}
