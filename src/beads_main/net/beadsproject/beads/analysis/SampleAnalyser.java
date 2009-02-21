/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
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

/**
 * SampleAnalyser handles feature analysis of a {@link Sample}. A feature extraction chain can be created by connecting various {@link FeatureExtractor}s to a {@link Segmenter}. SampleAnalyser uses feature extraction chains to analyse its given {@link Sample} using the method {@link #analyseSample(Segmenter)}, which takes the {@link Segmenter} as an argument.   
 *
 * @author ollie
 */
public class SampleAnalyser {

	/** The sample. */
	private Sample sample;
	
	/** The layers. */
	private Hashtable<String, FeatureLayer> layers;

	/**
	 * Instantiates a new SampleAnalyser.
	 * 
	 * @param sample the sample
	 */
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

	/**
	 * Reads the feature data from a file.
	 * 
	 * @param string the string
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	@SuppressWarnings("unchecked")
	public void readFeatures(String string) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(string);
		ObjectInputStream ois = new ObjectInputStream(fis);
		layers = (Hashtable<String, FeatureLayer>)ois.readObject();
		ois.close();
		fis.close();
	}

	/**
	 * Writes the feature data to a file.
	 * 
	 * @param string the file name.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeFeatures(String string) throws IOException {
		FileOutputStream fos = new FileOutputStream(string);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(layers);
		oos.close();
		fos.close();
	}

	/**
	 * Analyse the Sample using the given Segmenter. The type of feature extraction performed is specified by connecting FeatureExtractors to the Segmenter.
	 * 
	 * @param s the Segmenter.
	 */
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
	
	/**
	 * Prints the FeatureLayers.
	 */
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

	/**
	 * Gets the FeatureLayer with the given name.
	 * 
	 * @param key the name.
	 * 
	 * @return the FeatureLayer.
	 */
	public FeatureLayer getLayer(String key) {
		return layers.get(key);
	}

	/**
	 * Adds the specified FeatureLayer with the specified String as a reference..
	 * 
	 * @param key the name of the FeatureLayer.
	 * @param value the FeatureLayer.
	 */
	public void setLayer(String key, FeatureLayer value) {
		layers.put(key, value);
	}

}
