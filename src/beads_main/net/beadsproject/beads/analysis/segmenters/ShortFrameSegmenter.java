/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.segmenters;

import net.beadsproject.beads.analysis.Segmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.HanningWindow;

/**
 * A ShortFrameSegmenter slices audio data in short regular overlapping chunks.
 * 
 * @see Segmenter
 * 
 * @author ollie
 */
public class ShortFrameSegmenter extends Segmenter {

	/** The chunk size. */
	private int chunkSize;
	
	/** The hop size. */
	private int hopSize;
	
	/** The current chunks being recorded at the moment. */
	private float[][] chunks;
	
	/** The time in samples. */
	private int count;
	
	/** The window function used to scale the chunks. */
	private Buffer window;
	
	/**
	 * Instantiates a new ShortFrameSegmenter.
	 * 
	 * @param context the AudioContext.
	 */
	public ShortFrameSegmenter(AudioContext context) {
		super(context);
		hopSize = context.getBufferSize();
		chunkSize = hopSize * 2;
		window = new HanningWindow().getDefault();
		init();
	}
	
	/**
	 * Gets the chunk size.
	 * 
	 * @return the chunk size.
	 */
	public int getChunkSize() {
		return chunkSize;
	}
	
	/**
	 * Sets the chunk size.
	 * 
	 * @param chunkSize the new chunk size.
	 */
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
		init();
	}

	/**
	 * Gets the hop size.
	 * 
	 * @return the hop size.
	 */
	public int getHopSize() {
		return hopSize;
	}

	/**
	 * Sets the hop size.
	 * 
	 * @param hopSize the new hop size.
	 */
	public void setHopSize(int hopSize) {
		this.hopSize = hopSize;
		init();
	}
	
	/**
	 * Sets the window Buffer.
	 * 
	 * @param window the new window Buffer.
	 */
	public void setWindow(Buffer window) {
		this.window = window;
	}
	
	/**
	 * Resets the chunks array and count when anything affecting the chunk array gets changed.
	 */
	private void init() {
		int requiredBuffers = (int)Math.ceil((float)chunkSize / (float)hopSize);
		chunks = new float[requiredBuffers][chunkSize];
		count = 0;
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
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
