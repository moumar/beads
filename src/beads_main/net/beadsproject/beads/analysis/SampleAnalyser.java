package net.beadsproject.beads.analysis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.SamplePlayer;

// TODO: Work out how this would do realtime analysis
public class SampleAnalyser {

	private Sample sample;
	private Hashtable<String, FeatureLayer> layers;

	public SampleAnalyser(Sample sample) {
		this.sample = sample;
		if(sample.getFileName() != null) {
			try {
				readFeatures(sample.getFileName() + ".features");
			} catch(Exception e) {
				layers = new Hashtable<String, FeatureLayer>();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void readFeatures(String string) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(string);
		ObjectInputStream ois = new ObjectInputStream(fis);
		layers = (Hashtable<String, FeatureLayer>)ois.readObject();
		ois.close();
		fis.close();
	}

	public void writeFeatures(String string) throws IOException {
		FileOutputStream fos = new FileOutputStream(string);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(layers);
		oos.close();
		fos.close();
	}

	public void analyseSample(Segmenter s) {
		s.setFeatureLayer(new FeatureLayer());
		AudioContext ac = s.getContext();
		SamplePlayer sp = new SamplePlayer(ac, sample);
		s.addInput(sp);
		ac.out.addDependent(s);
		sp.setKillListener(new AudioContextStopTrigger(ac));
		ac.runNonRealTime();
		setLayer(s.toString(), s.getFeatureLayer());
		if(sample.getFileName() != null) {
			try {
				writeFeatures(sample.getFileName() + ".features");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void printLayers() {
		for(String s : layers.keySet()) {
			System.out.println("Layer Name");
			System.out.println(s);
			System.out.println("Number of Frames: " + layers.size());
			FeatureLayer fl = layers.get(s);
			for(FeatureFrame ff : fl) {
				System.out.println(ff);
			}
		}
	}

	public FeatureLayer getLayer(String key) {
		return layers.get(key);
	}

	public void setLayer(String key, FeatureLayer value) {
		layers.put(key, value);
	}

}
