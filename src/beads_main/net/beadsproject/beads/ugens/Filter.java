/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;
import java.util.Arrays;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Static;


public class Filter extends UGen {

	private UGen alphaEnvelope;
	float[] lastValue;
	float alpha;

	public Filter(AudioContext context, int inouts) {
		this(context, inouts, 0.5f);
	}
	

	public Filter(AudioContext context, int inouts, float alpha) {
		this(context, inouts, new Static(context, alpha));
	}

	public Filter(AudioContext context, int inouts, UGen cutoffEnvelope) {
		super(context, inouts, inouts);
		lastValue = new float[inouts];
		Arrays.fill(lastValue, 0f);
		setAlphaEnvelope(cutoffEnvelope);
	}

	public void setAlphaEnvelope(UGen cutoffEnvelope) {
		this.alphaEnvelope = cutoffEnvelope;
	}

	public UGen getAlphaEnvelope() {
		return alphaEnvelope;
	}

	@Override
	public void calculateBuffer() {
		alphaEnvelope.update();
		for(int i = 0; i < ins; i++) {
			for(int j = 0; j < bufferSize; j++) {
				bufOut[i][j] = lastValue[i] + alphaEnvelope.getValue(0, j) * (bufIn[i][j] - lastValue[i]);
				lastValue[i] = bufOut[i][j];
			}
		}
	}


}