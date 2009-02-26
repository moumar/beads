package net.beadsproject.beads.analysis.featureextractors;

import java.io.FileOutputStream;
import java.io.PrintStream;

import net.beadsproject.beads.analysis.FeatureExtractor;

public class GnuplotDataWriter extends FeatureExtractor<float[], float[]> {

	private PrintStream ps;
	private int count;
	
	public GnuplotDataWriter(FileOutputStream fos) {
		ps = new PrintStream(fos);
		count = 0;
	}
	
	@Override
	public void process(float[] data) {
		for(int i = 0; i < data.length; i++) {
			ps.println(count + " " + i + " " + data[i]);
		}
		ps.println();
		count++;
	}

}
