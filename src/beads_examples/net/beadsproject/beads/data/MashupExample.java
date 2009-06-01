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
	private long previousTimeMS;
	
	public MashupExample(String dir) {
		maxSamples = 200;
		samples = new Hashtable<String, Sample>();
		features = new Hashtable<String, FeatureTrack>();
		previousTimeMS = System.currentTimeMillis();
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
					Sample samp = new Sample(f.getAbsolutePath(), 
							new Sample.TimedRegime(100,0,0,5000,Sample.TimedRegime.Order.NEAREST));
					samples.put(f.getAbsolutePath(), samp);
					long timeMS = System.currentTimeMillis();
					float timeTakenMS = (timeMS - previousTimeMS);
					previousTimeMS = timeMS;
					System.out.println("Loaded " + f.getAbsolutePath() + " in " + timeTakenMS + "ms");
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
		// new MashupExample("/Users/ollie/Music/iTunes/iTunes Music/Unknown Artist/Unknown Album");
		new MashupExample("D:/Music/Mozart Discography (5 CDs) 320kbps/Mozart - Concertos for Flute");
	}
}
