/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileOutputStream;
import java.io.PrintStream;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.AudioContext;

/**
 * BasicTimeDataWriter grabs forwarded feature data and prints it to a file in a simple format.
 * Each line contains the running time of the AC and a set of features at that timestep.
 * Each individual feature is separated by whitespace.
 */
public class BasicTimeDataWriter extends FeatureExtractor<float[], float[]> {

	/** The print stream. */
	private PrintStream ps;
	private AudioContext ac;
	
	/**
	 * Instantiates a new BasicDataWriter with the given FileOutputStream.
	 * 
	 * @param fos the FileOutputStream.
	 */
	public BasicTimeDataWriter(AudioContext ac, FileOutputStream fos) {
		this.ac = ac;
		ps = new PrintStream(fos);
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.analysis.FeatureExtractor#process(java.lang.Object)
	 */
	@Override
	public void process(float[] data) {
		ps.print(ac.getTime());
		ps.print(" ");		 
		for(int i = 0; i < data.length; i++) {
			ps.print(data[i]);
			ps.print(" ");			
		}
		ps.println();		
	}

}
