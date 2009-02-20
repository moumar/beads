package net.beadsproject.beads.analysis;

import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.Recorder;


public abstract class Segmenter extends UGen {

	//limitation: the segmenter cannot do stuff at a shorter resolution than the buffer size
	//requirement: you have to implement the recorder if you want to gather audio and pass it on to your listeners
	
	private FeatureLayer fl;
	private ArrayList<FeatureExtractor> responders;
	private BeadArray listeners;
	private ArrayList<FeatureExtractor> extractors;
	protected double currentTime, previousEndTime;
	
	public Segmenter(AudioContext context) {
		super(context, 1, 0);		
		responders = new ArrayList<FeatureExtractor>();
		extractors = new ArrayList<FeatureExtractor>();
		listeners = new BeadArray();
		reset(0);
	}
	
	public void reset(double time) {
		setTime(time);
	}
	
	public void setTime(double time) {
		currentTime = previousEndTime = time;
	}

	public void setFeatureLayer(FeatureLayer fl) {
		this.fl = fl;
	}
	
	public FeatureLayer getFeatureLayer() {
		return fl;
	}
	
	public void unsetFeatureLayer() {
		fl = null;
	}
	
	public void addResponder(FeatureExtractor fe) {
		responders.add(fe);
	}
	
	public void addExtractor(FeatureExtractor fe) {
		extractors.add(fe);
	}
	
	public void addResponderExtractor(FeatureExtractor fe) {
		addResponder(fe);
		addExtractor(fe);
	}
	
	public void addListener(Bead bead) {
		listeners.add(bead);
	}
	
	protected void segment(float[] data, int length) {
		for(FeatureExtractor fe : responders) {
			fe.process(data, length);
		}
		if(fl != null) {
			FeatureFrame ff = new FeatureFrame(previousEndTime, currentTime);
			for(FeatureExtractor fe : extractors) {
				ff.add(fe.getName(), fe.getFeatures());
			}
			fl.add(ff);
			previousEndTime = currentTime;
			currentTime += context.samplesToMs(length);
		}
		listeners.message(this);
	}
	
	public String toString() {
		String result = "Segmenter: " + getClass().getSimpleName();
		for(FeatureExtractor fe : extractors) {
			result += "\n    " + fe.getName();
		}
		return result;
	}
	
}
