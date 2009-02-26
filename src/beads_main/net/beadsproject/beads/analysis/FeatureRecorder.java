package net.beadsproject.beads.analysis;

import java.util.ArrayList;
import java.util.List;
import net.beadsproject.beads.core.Bead;


public class FeatureRecorder extends Bead {

	private FeatureTrack currentTrack;
	
	private List<FeatureExtractor> extractors;
	
	public FeatureRecorder() {
		currentTrack = new FeatureTrack();
		extractors = new ArrayList<FeatureExtractor>();
	}
	
	public void addFeatureExtractor(FeatureExtractor e) {
		extractors.add(e);
	}
	
	public void removeFeatureExtractor(FeatureExtractor e) {
		extractors.remove(e);
	}
	
	public void messageReceived(Bead bead) {
		System.out.println("x");
		Segmenter s = (Segmenter)bead;
		FeatureFrame ff = new FeatureFrame(s.previousEndTime, s.currentTime);
		for(FeatureExtractor e : extractors) {
			ff.add(e.getName(), e.getFeatures());
		}
		currentTrack.add(ff);
	}

	public void setFeatureTrack(FeatureTrack ft) {
		currentTrack = ft;
	}
	
	public FeatureTrack getFeatureTrack() {
		return currentTrack;
	}
	
	
}
