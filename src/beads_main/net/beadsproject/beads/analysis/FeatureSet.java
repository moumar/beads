package net.beadsproject.beads.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;


public class FeatureSet {

	private Hashtable<String, FeatureTrack> tracks;
	private File file;

	public FeatureSet(File file) {
		this();
		read(file);
	}
	
	public FeatureSet() {
		tracks = new Hashtable<String, FeatureTrack>();
	}
	
	public FeatureTrack get(String trackName) {
		return tracks.get(trackName);
	}
	
	public void add(String trackName, FeatureTrack track) {
		tracks.put(trackName, track);
	}
	
	public void write() {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(tracks);
			oos.close();
			fos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void read(File file) {
		this.file = file;
		read();
	}
	
	@SuppressWarnings("unchecked")
	private void read() {
		if(file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fis);
				tracks = (Hashtable<String, FeatureTrack>)ois.readObject();
				ois.close();
				fis.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void refresh() {
		read();
	}
	
	public void write(File file) {
		this.file = file;
		write();
	}
	
}
