package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.SampleManager;


public class SimpleReverb extends UGen {

	//use a standard tap in
	private TapIn[] tin;
	//but develop own tap outs
	private Gain[] feedback;
	private TapOut[][] tapOuts;
	private int numTaps;
	private float randomSpread;
	private float preDelay;
	private float tapSpread;
	//defaults
	
	public SimpleReverb(AudioContext context, int inouts) {
		super(context, inouts, inouts);
		numTaps = 10;
		randomSpread = 10f;
		preDelay = 50f;
		tapSpread = 30f;
		tin = new TapIn[inouts];
		feedback = new Gain[inouts];
		tapOuts = new TapOut[inouts][];
		recalculateEverything();
	}
	
	private void recalculateEverything() {
		for(int i = 0; i < tin.length; i++) {
			tin[i] = new TapIn(context, 10000f);
			feedback[i] = new Gain(context, 1, 1f);
			tin[i].addInput(feedback[i]);
			tapOuts[i] = new TapOut[numTaps];
			for(int j = 0; j < numTaps; j++) {
				tapOuts[i][j] = new TapOut(context, tin[i], j * tapSpread + preDelay + (float)Math.random() * randomSpread);
				float decay = (float)Math.exp(-j / 200f) / numTaps;
				Gain g = new Gain(context, 1, decay);
				g.addInput(tapOuts[i][j]);
				feedback[i].addInput(g);
			}
		}
	}
	
	private void recalculateDelays() {
		for(int i = 0; i < tapOuts.length; i++) {
			for(int j = 0; j < tapOuts[i].length; j++) {
				tapOuts[i][j].getSampleDelayEnvelope().setValue(j * tapSpread + preDelay + (float)Math.random() * randomSpread);
			}
		}
	}
	
	public void addInput(int inport, UGen source, int outport) {
		tin[inport].addInput(0, source, outport);
	}
	
	public void removeAllConnections(UGen source) {
		for(int i = 0; i < tin.length; i++) {
			tin[i].removeAllConnections(source);
		}
	}

	@Override
	public void calculateBuffer() {
		//just update the feedback UGens and get their output
		for(int i = 0; i < outs; i++) {
			feedback[i].update();
			for(int j = 0; j < bufferSize; j++) {
				bufOut[i][j] = feedback[i].getValue(0, j);
			}
		}
	}
	
	public int getNumTaps() {
		return numTaps;
	}

	
	public void setNumTaps(int numTaps) {
		this.numTaps = numTaps;
		recalculateEverything();
	}

	
	public float getRandomSpread() {
		return randomSpread;
	}

	
	public void setRandomSpread(float randomSpread) {
		this.randomSpread = randomSpread;
		recalculateDelays();
	}

	
	public float getPreDelay() {
		return preDelay;
	}

	
	public void setPreDelay(float preDelay) {
		this.preDelay = preDelay;
		recalculateDelays();
	}

	
	public float getTapSpread() {
		return tapSpread;
	}

	
	public void setTapSpread(float tapSpread) {
		this.tapSpread = tapSpread;
		recalculateDelays();
	}

	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
		SimpleReverb rb = new SimpleReverb(ac, 2);
		rb.addInput(sp);
		ac.out.addInput(rb);
		ac.out.addInput(sp);
		ac.start();
	}

	
}
