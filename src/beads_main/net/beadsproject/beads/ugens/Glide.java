package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.buffers.SineBuffer;

public class Glide extends UGen {

	private float currentValue;
	private float previousValue;
	private float targetValue;
	private int glideTime; //in samples
	private int countSinceGlide;
	private boolean gliding;
	private boolean nothingChanged;
	private float[] bufOut;
	
	public Glide(AudioContext context, float currentValue) {
		super(context, 1);
		this.currentValue = currentValue;
		glideTime = (int)context.msToSamples(100f);
		countSinceGlide = 0;
		gliding = false;
		nothingChanged = false;
		outputInitializationRegime = OutputInitializationRegime.NULL;
		bufOut = new float[bufferSize];
	}
	
	public Glide(AudioContext context) {
		this(context, 0f);
	}

	public void setValue(float targetValue) {
		this.targetValue = targetValue;
		gliding = true;
		nothingChanged = false;
		countSinceGlide = 0;
		previousValue = currentValue;
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
					if(countSinceGlide >= glideTime) {
						gliding = false;
						bufOut[i] = previousValue = targetValue;
					} else {
						float offset = ((float)countSinceGlide / glideTime);
						bufOut[i] = currentValue = offset * targetValue + (1f - offset) * previousValue;
						nothingChanged = false;
					}
					countSinceGlide++;
				} else {
					bufOut[i] = currentValue;
				}
			}
		}
	}
	
	public float getValue(int i, int j) {
		return bufOut[j];
	}

	public static void main(String args[]) {
		AudioContext ac = new AudioContext();
		final Glide g = new Glide(ac, 500) {
			public void calculateBuffer() {
				super.calculateBuffer();
//				setValue((float)Math.random() * 10000 + 100);
//				setValue(1000f);
				System.out.println(getValue());
			}
		};
		WavePlayer wp = new WavePlayer(ac, g, new SineBuffer().getDefault());
		ac.out.addInput(wp);
		ac.start();
		Thread t = new Thread() {
			public void run() {
				while(true) {
					g.setValue((float)Math.random() * 10000 + 100);
					try {
						sleep(50);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}
}
