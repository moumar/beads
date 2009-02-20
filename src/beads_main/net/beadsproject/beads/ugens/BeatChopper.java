package net.beadsproject.beads.ugens;

import java.util.ArrayList;
import java.util.Hashtable;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;


public class BeatChopper extends Gain {

	private Sample s;
	private SamplePlayer sp;
	private float sampleTempo;
	private float realTempo;
	private ArrayList<ArrayList<Float>> onsets;
	int depth;
	private float[] triggerProbs;
	
	public BeatChopper(AudioContext context, Sample s) {
		super(context, s.nChannels);
		sp = new SamplePlayer(context, s);
		addInput(sp);
		sampleTempo = 120f;
		realTempo = 120f;
		depth = 8;
		triggerProbs = new float[] {0.125f, 0.25f, 0f, 0.5f, 0f, 0f, 0f, 0.8f};
	}
	
	public void setSampleTempo(float sampleTempo) {
		this.sampleTempo = sampleTempo;
	}
	
	public void getOnsetsFromTempo() {
		float time = 0;
		int index = 0;
		onsets = new ArrayList<ArrayList<Float>>();
		for(int i = 0; i < depth; i++) {
			onsets.add(new ArrayList<Float>());
		}
		float semiQuaverInterval = 60000f / sampleTempo / 4f;
		while(time < s.length) {
			for(int i = 0; i < depth; i++) {
				if(index % i == 0) {
					onsets.get(i).add(time);
				}
			}
			time += semiQuaverInterval;
			index++;
		}
	}

	public void message(Clock message) {
		int count = message.getCount();
		int thisDepth;
		for(thisDepth = depth; thisDepth > 0; depth--) {
			if(count % thisDepth == 0) break;
		}
	}
}
