package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.SampleManager;

/**
 * @beads.category effect
 */
public class SimpleReverb extends UGen {

	//use a standard tap in
	private TapIn[] tin;
	//put range limiters before tap in, keep things in range [-1,1]
	private RangeLimiter[] rl;
	//and tap outs, with feedback gains
	private Gain[] feedback;
	private TapOut[][] tapOuts;
	private int numTaps;
	private float randomSpread;
	private float preDelay;
	private float tapSpread;
	private float feedbackGain;
	private float delayLength;
	private float outputGain;
	//defaults
	
	public SimpleReverb(AudioContext context, int inouts) {
		super(context, inouts, inouts);
		numTaps = 10;
		randomSpread = 10f;
		preDelay = 50f;
		delayLength = 300f;
		feedbackGain = 0.9f;
		outputGain = 1f;
		rl = new RangeLimiter[inouts];
		tin = new TapIn[inouts];
		feedback = new Gain[inouts];
		tapOuts = new TapOut[inouts][];
		for(int i = 0; i < tin.length; i++) {
			tin[i] = new TapIn(context, 10000f);
			rl[i] = new RangeLimiter(context, 1);
			tin[i].addInput(rl[i]);
			feedback[i] = new Gain(context, 1, feedbackGain);
			rl[i].addInput(feedback[i]);
		}
		recalculateEverything();
	}
	
	private void recalculateEverything() {
		tapSpread = delayLength / numTaps;
		for(int i = 0; i < tin.length; i++) {
			feedback[i].clearInputConnections();
			tapOuts[i] = new TapOut[numTaps];
			for(int j = 0; j < numTaps; j++) {
				float tapDelay = j * tapSpread + preDelay + (float)Math.random() * randomSpread;
				tapOuts[i][j] = new TapOut(context, tin[i], tapDelay);
				float decay = (float)Math.exp(-j / 100f) / numTaps;
				Gain g = new Gain(context, 1, decay);
				g.addInput(tapOuts[i][j]);
				feedback[i].addInput(g);
			}
		}
	}
	
	public void addInput(int inport, UGen source, int outport) {
		rl[inport].addInput(0, source, outport);
	}
	
	public void removeAllConnections(UGen source) {
		for(int i = 0; i < rl.length; i++) {
			rl[i].removeAllConnections(source);
		}
	}

	@Override
	public void calculateBuffer() {
		//just update the feedback UGens and get their output
		for(int i = 0; i < outs; i++) {
			feedback[i].update();
			for(int j = 0; j < bufferSize; j++) {
				bufOut[i][j] = outputGain * feedback[i].getValue(0, j);
			}
		}
	}
	
	public int getNumTaps() {
		return numTaps;
	}

	
	public void setNumTaps(int numTaps) {
		this.numTaps = Math.max(1, numTaps);
		recalculateEverything();
	}

	
	public float getRandomSpread() {
		return randomSpread;
	}

	
	public void setRandomSpread(float randomSpread) {
		this.randomSpread = randomSpread;
		recalculateEverything();
	}

	
	public float getPreDelay() {
		return preDelay;
	}

	
	public void setPreDelay(float preDelay) {
		this.preDelay = preDelay;
		recalculateEverything();
	}
	
	public float getDelayLength() {
		return delayLength;
	}

	
	public void setDelayLength(float delayLength) {
		this.delayLength = delayLength;
	}

	public float getFeedbackGain() {
		return feedbackGain;
	}

	
	public void setFeedbackGain(float feedbackGain) {
		this.feedbackGain = feedbackGain;
		for(int i = 0; i < feedback.length; i++) {
			feedback[i].getGainEnvelope().setValue(feedbackGain);
		}
	}
	
	public float getOutputGain() {
		return outputGain;
	}
	
	public void setOutputGain(float outputGain) {
		this.outputGain = outputGain;
	}

	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		
		float gainPerversion = 1f;
		
		//up gain and down gain -- just doing this coz there's some bug where the reverb wipes out, seeing if it's something to do
		//with the level coming in
		Gain upGain = new Gain(ac, 2, gainPerversion);
		
		for(int i = 0; i < 1; i++) {
//			SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
			SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("/Users/ollie/Music/Audio/classic breaks/funkyd.aiff"));
			sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
			sp.getRateEnvelope().setValue((float)Math.random() + 1f);
			upGain.addInput(sp);
		}
		
		SimpleReverb rb = new SimpleReverb(ac, 2);
		rb.addInput(upGain);
		
		Gain downGain = new Gain(ac, 2, 1f / gainPerversion);
		downGain.addInput(rb);
		downGain.addInput(upGain);
		
//		RangeLimiter rl = new RangeLimiter(ac, 2);
//		rl.addInput(upG)
		
		ac.out.addInput(downGain);
		
//		rb.setFeedbackGain(0.9f);
//		rb.setNumTaps(20);
//		rb.setDelayLength(300f);
//		rb.setOutputGain(2f);
		ac.start();
	}

	
}
