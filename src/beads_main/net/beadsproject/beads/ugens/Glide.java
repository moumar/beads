package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

public class Glide extends UGen {

	private float currentValue;
	private float targetValue;
	private int glideTime; //in samples
	private int countSinceGlide;
	private boolean gliding;
	private boolean nothingChanged;
	
	public Glide(AudioContext context, float currentValue) {
		super(context, 1);
		this.currentValue = currentValue;
		targetValue = 0f;
		glideTime = (int)context.msToSamples(100f);
		countSinceGlide = 0;
		gliding = false;
		nothingChanged = false;
	}
	
	public Glide(AudioContext context) {
		this(context, 0f);
	}
	
	public void setValue(float targetValue) {
		this.targetValue = targetValue;
		gliding = true;
		nothingChanged = false;
		countSinceGlide = 0;
	}
	
	public void setGlideTime(float msTime) {
		glideTime = (int)context.msToSamples(msTime);
	}
	
	public float getGlideTime() {
		return (float)context.samplesToMs(glideTime);
	}

	@Override
	public void calculateBuffer() {
		if(!nothingChanged) {
			nothingChanged = true;
			for(int i = 0; i < bufferSize; i++) {
				if(gliding) {
					float offset = ((float)countSinceGlide / glideTime);
					bufOut[0][i] = offset * targetValue + (1f - offset) * currentValue;
					if(countSinceGlide > glideTime) {
						gliding = false;
						currentValue = bufOut[0][i];
					} else {
						nothingChanged = false;
					}
					countSinceGlide++;
				} else {
					bufOut[0][i] = currentValue;
				}
			}
		}
	}

	
	
}
