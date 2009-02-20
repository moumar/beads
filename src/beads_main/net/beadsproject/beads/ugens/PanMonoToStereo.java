package net.beadsproject.beads.ugens;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Static;



public class PanMonoToStereo extends UGen {

	public UGen panEnvelope;
	
	public PanMonoToStereo(AudioContext context) {
		this(context, new Static(context, 0.5f));
	}
	
	public PanMonoToStereo(AudioContext context, UGen panEnvelope) {
		super(context, 1, 2);
		setPanEnvelope(panEnvelope);
	}

	public void setPanEnvelope(UGen panEnvelope) {
		this.panEnvelope = panEnvelope;
	}
	
	public UGen getPanEnvelope() {
		return panEnvelope;
	}

	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			panEnvelope.update();
			float pan = panEnvelope.getValue(0, i);
			if(pan < 0) pan = 0;
			else if(pan > 1) pan = 1;
			bufOut[0][i] = bufIn[0][i] * pan;
			bufOut[1][i] = bufIn[0][i] * (1f - pan);
		}
	}
	
}
