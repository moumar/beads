package net.beadsproject.beads.analysis.segmenters;

import net.beadsproject.beads.analysis.Segmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.HanningWindow;


public class ShortFrameSegmenter extends Segmenter {

	int chunkSize;
	int hopSize;
	float[][] chunks;
	int count;
	Buffer window;
	
	public ShortFrameSegmenter(AudioContext context) {
		super(context);
		hopSize = context.getBufferSize();
		chunkSize = hopSize * 2;
		window = new HanningWindow().getDefault();
		init();
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
		init();
	}

	public int getHopSize() {
		return hopSize;
	}

	public void setHopSize(int hopSize) {
		this.hopSize = hopSize;
		init();
	}
	
	public void setHanningWindow(Buffer window) {
		this.window = window;
	}
	
	public void init() {
		int requiredBuffers = (int)Math.ceil((float)chunkSize / (float)hopSize);
		chunks = new float[requiredBuffers][chunkSize];
		count = 0;
	}

	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			for(int j = 0; j < chunks.length; j++) {
				int pos = (count + i * hopSize) % chunkSize;
				chunks[j][pos] = bufIn[0][i] * window.getValueFraction((float)pos / (float)chunkSize);
			}
			count = (count + 1) % chunkSize;
			if(count % hopSize == 0) {
				segment(chunks[count / hopSize], chunkSize);
			}
		}
	}

}











