/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileOutputStream;
import java.io.PrintStream;

import net.beadsproject.beads.analysis.FeatureExtractor;

/**
 * GnuplotDataWriter grabs forwarded feature data and prints it to a file in pm3d format for Gnuplot.
 */
public class GnuplotDataWriter extends FeatureExtractor<float[], float[]> {

	/** The print stream. */
	private PrintStream ps;
	
	/** The current integer frame count. */
	private int count;
	
	/**
	 * Instantiates a new GnuplotDataWriter with the given FileOutputStream.
	 * 
	 * @param fos the FileOutputStream.
	 */
	public GnuplotDataWriter(FileOutputStream fos) {
		ps = new PrintStream(fos);
		count = 0;
	}
	
	/* (non-Javadoc)
	 * @see net.beadsproject.beads.analysis.FeatureExtractor#process(java.lang.Object)
	 */
	@Override
	public void process(float[] data) {
		for(int i = 0; i < data.length; i++) {
			ps.println(count + " " + i + " " + data[i]);
		}
		ps.println();
		count++;
	}

}
