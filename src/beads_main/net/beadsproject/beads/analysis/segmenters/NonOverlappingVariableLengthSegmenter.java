package net.beadsproject.beads.analysis.segmenters;

import net.beadsproject.beads.analysis.Segmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.TimeStamp;

/**
 * A Segmenter that responds to a timeStamped message by creating a segment of audio since the previous message.
 * @author ollie
 *
 */
public abstract class NonOverlappingVariableLengthSegmenter extends Segmenter {

	private float[] data;
	private int index;
	private int currentLength;
	
	public NonOverlappingVariableLengthSegmenter(AudioContext context, int maxSamples) {
		super(context);
		data = new float[maxSamples];
		index = 0;
		currentLength = 0;
	}
	
	public synchronized void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			data[index] = bufIn[0][i];
			index = (index + 1) % data.length;
			currentLength++;
		}
	}
	
	public synchronized void messageReceived(TimeStamp timeStamp, Bead message) {
		//confirm that we're in the correct time step (so that we can grab this audio)
		assert isUpdated();
		
		//FIXME - this is wrong
		//TODO - also some windowing and prep for FFT
		
		int distanceFromEnd = bufferSize - timeStamp.index;
		int length = currentLength - distanceFromEnd;
		//limit length to max data length
		float[] newBlock = new float[Math.min(length, data.length)];
		//fill block with data
		for(int i = 0; i < newBlock.length; i++) {
			int dataIndex = index - distanceFromEnd - newBlock.length + i;
			dataIndex = (dataIndex % data.length + data.length) % data.length;
			newBlock[i] = data[dataIndex];
		}
		
		/////////////////////
		
		//forward it to receivers
//		segment(startTime, endTime, newBlock);	//TODO - sort this out
	}
	
	

}
