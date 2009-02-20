package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;

public class Breakbeat extends Gain {

	private final GranularSamplePlayer gsp;
	
	public Breakbeat(AudioContext context, Sample s, final float sampleInterval, UGen masterIntervalEnvelope) {
		super(context, s.nChannels);
		gsp = new GranularSamplePlayer(context, s);
		UGen rateCalculator = new UGen(context, 1, 1) {
			@Override
			public void calculateBuffer() {
				for(int i = 0; i < bufferSize; i++) {
					bufOut[0][i] =  sampleInterval/ bufIn[0][i];
				}
			}
		};
		rateCalculator.addInput(masterIntervalEnvelope);
		gsp.setRateEnvelope(rateCalculator);
		gsp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		gsp.getLoopStartEnvelope().setValue(0f);
		gsp.getLoopEndEnvelope().setValue(s.length);
		System.out.println(s.length);
		addInput(gsp);	
		setGainEnvelope(new Envelope(context, 1f));
	}
	
	public void reTrigger() {
		gsp.reTrigger();
		pause(false);
	}
	
	public GranularSamplePlayer getGSP() {
		return gsp;
	}

	@Override
	public Envelope getGainEnvelope() {
		return (Envelope)super.getGainEnvelope();
	}

	public void setPosition(float f) {
		gsp.setPosition(f);
	}

}
