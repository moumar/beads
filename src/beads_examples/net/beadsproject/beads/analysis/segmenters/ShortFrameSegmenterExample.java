package net.beadsproject.beads.analysis.segmenters;

import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Static;
import net.beadsproject.beads.ugens.WavePlayer;

public class ShortFrameSegmenterExample {
	
	public static void main(String[] args) {
		AudioContext ac = new AudioContext(512);
		ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac) {
			public void segment(TimeStamp startTime, TimeStamp endTime, float[] data) {
				super.segment(startTime, endTime, data);
				for(int i = 0; i < data.length; i++) {
					System.out.print(data[i] + " ");
				}
				System.out.println();
			}
		};
		sfs.setChunkSize(10);
		sfs.setHopSize(20);
//		WavePlayer wp = new WavePlayer(ac, 500f, new SineBuffer().getDefault());
//		sfs.addInput(wp);
		Static s = new Static(ac, 1);
		sfs.addInput(s);
		ac.out.addDependent(sfs);
//		ac.out.addInput(wp);
		ac.start();
	}

}
