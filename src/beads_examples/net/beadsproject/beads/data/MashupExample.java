package net.beadsproject.beads.data;

import java.io.File;
import java.util.Hashtable;
import net.beadsproject.beads.analysis.FeatureTrack;

/*
 * A lengthly example that goes through a folder of mp3s, does some basic analysis, and produces a pretty random mashup.
 */
public class MashupExample {

	private Hashtable<String, Sample> samples;
	private Hashtable<String, FeatureTrack> features;
	private int maxSamples;
	
	public MashupExample(String dir) {
		maxSamples = 200;
		samples = new Hashtable<String, Sample>();
		features = new Hashtable<String, FeatureTrack>();
		loadSamples(new File(dir));
		System.out.println("samples loaded: " + samples.size());
	}
	
	public void loadSamples(File subdir) {
		if(samples.size() > maxSamples) return;
		File[] files = subdir.listFiles();
		for(File f : files) {
			if(samples.size() > maxSamples) break;
			if(f.isDirectory()) {
				loadSamples(f);
			} else {
				try {
					Sample s = new Sample();
					s.setBufferingRegime(Sample.BufferingRegime.TIMED);
					s.setRegionSize(100);
					s.setLookAhead(0);
					s.setLookBack(0);
					s.setMemory(5000);
					s.setFile(f.getAbsolutePath());
					samples.put(f.getAbsolutePath(), s);
					System.out.println("Loaded " + f.getAbsolutePath());
				} catch(Exception e) {
					System.out.println("Didn't load " + f.getAbsolutePath());
//					e.printStackTrace();
					//do nothing
				}
			}
		}
	}
	
	public void buildAnalysisDatabase() {
		
	}
	
	public static void main(String[] args) {
		//use a folder full of audio
		new MashupExample("/Users/ollie/Music/iTunes/iTunes Music");
	}
}
