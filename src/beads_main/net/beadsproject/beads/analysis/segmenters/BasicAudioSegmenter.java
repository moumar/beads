package net.beadsproject.beads.analysis.segmenters;

import net.beadsproject.beads.analysis.AudioSegmenter;
import net.beadsproject.beads.analysis.SegmentListener;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.HanningWindow;

/**
 * An AudioSegmenter that sends slices of audio in response to messages from a SegmentMaker.
 * @author ollie
 *
 */
public class BasicAudioSegmenter extends AudioSegmenter implements SegmentListener {

	private float[] data;
	private int addIndex;
	private int currentLength;
	private Buffer window;
	
	public BasicAudioSegmenter(AudioContext context, int maxSamples, Buffer window) {
		super(context);
		data = new float[maxSamples];
		addIndex = 0;
		currentLength = 0;
		this.window = window;
	}
	
	public BasicAudioSegmenter(AudioContext context) {
		this(context, (int)context.msToSamples(1000), new HanningWindow().getDefault());
	}
	
	public synchronized void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			data[addIndex] = bufIn[0][i];
			addIndex = (addIndex + 1) % data.length;
			currentLength++;
		}
	}

	public void newSegment(TimeStamp startTime, TimeStamp endTime) {
		//confirm that we're in the correct time step (so that we can grab this audio)
		assert isUpdated();
		int startIndex = indexFor(startTime);
		int length = (int)(endTime.getTimeSamples() - startTime.getTimeSamples());
		//limit length to max data length
		float[] newBlock = new float[Math.min(length, data.length)];
		//fill block with data
		for(int i = 0; i < newBlock.length; i++) {
			int dataIndex = (startIndex + i) % data.length;
			newBlock[i] = data[dataIndex] * window.getValueFraction((float)i / newBlock.length);
		}
		//forward it to receivers
		segment(startTime, endTime, newBlock);
	}

	private int indexFor(TimeStamp ts) {
		int samplesSinceTs = (int)(context.getTimeStep() - ts.timeStep) * bufferSize + bufferSize - ts.index;
		int index = currentLength - samplesSinceTs;
		while(index < 0) index += data.length;
		return index;
	}
	
	

}
