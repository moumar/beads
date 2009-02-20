package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

public class PanStereoToStereo extends UGen {

	private UGen panEnvelope;

	public PanStereoToStereo(AudioContext context) {
		super(context, 2, 2);
		panEnvelope = new Static(context, 0.5f);
	}

	@Override
	public void calculateBuffer() {
		panEnvelope.update();
		for(int i = 0; i < bufferSize; i++) {
			float pan = Math.max(0, Math.min(1, panEnvelope.getValue(0, i)));
			if(pan < 0.5f) {
				bufOut[0][i] = 2f * bufIn[0][i] * pan;
				bufOut[1][i] = bufIn[1][i];	
			} else {
				bufOut[0][i] = bufIn[0][i];
				bufOut[1][i] = 2f * bufIn[1][i] * (1f - pan);	
			}
			
			//yes I know this is a lazy panning algorithm. I am a lazy man.
		}
	}

	public UGen getPanEnvelope() {
		return panEnvelope;
	}

	public void setPanEnvelope(UGen panEnvelope) {
		this.panEnvelope = panEnvelope;
	}
	
}
