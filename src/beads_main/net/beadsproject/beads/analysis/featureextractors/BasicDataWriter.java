/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileOutputStream;
import java.io.PrintStream;

import net.beadsproject.beads.analysis.FeatureExtractor;

/**
 * BasicDataWriter grabs forwarded feature data and prints it to a file in a simple format.
 * Each line contains a new set of features.
 * Each individual feature is separated by whitespace.
 */
public class BasicDataWriter extends FeatureExtractor<float[], float[]> {

	/** The print stream. */
	private PrintStream ps;
	
	/**
	 * Instantiates a new BasicDataWriter with the given FileOutputStream.
	 * 
	 * @param fos the FileOutputStream.
	 */
	public BasicDataWriter(FileOutputStream fos) {
		ps = new PrintStream(fos);
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.analysis.FeatureExtractor#process(java.lang.Object)
	 */
	@Override
	public void process(float[] data) {
		for(int i = 0; i < data.length; i++) {
			ps.print(data[i]);
			ps.print(" ");			
		}
		ps.println();		
	}

}
