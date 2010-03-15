package net.beadsproject.beads.core.io;

import java.nio.FloatBuffer;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.UGen;
import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackSystem;

public class JJackAudioIO extends AudioIO {
	
	public final static int RING_BUFFER_SIZE = 10000;
	
	private int jjackInputIndex;
	private int beadsInputIndex;
	private int jjackOutputIndex;
	private int beadsOutputIndex;
	private float timeSinceUpdate;
	private boolean firstUpdateComplete;
	/*
	 * simple ring buffers for each channel
	 */
	private float[][] inputs;	//float[channels][length]
	private float[][] outputs;  //float[channels][length]
	/*
	 * arrays for each jjack channel, may not be necessary but 
	 * likely that FloatBuffers are best accessed in batches.
	 */
	private float[][] jjackInputs; //float[channels][length]
	private float[][] jjackOutputs; //float[channels][length]
	
	@Override
	protected UGen getAudioInput(int[] channels) {
		return new JJackRTInput(context, channels);
	}

	@Override
	protected boolean start() {
		//check the buffer size
		final int jjBufSize = JJackSystem.getBufferSize();
		//set up arrays
		inputs = new float[context.getInputAudioFormat().getChannels()][RING_BUFFER_SIZE];
		outputs = new float[context.getAudioFormat().getChannels()][RING_BUFFER_SIZE];
		jjackInputs = new float[context.getInputAudioFormat().getChannels()][jjBufSize];	//this is a problem if channels are not the same
		jjackOutputs = new float[context.getAudioFormat().getChannels()][jjBufSize];	//this is a problem if channels are not the same
		jjackInputIndex = jjackOutputIndex = beadsInputIndex = beadsOutputIndex = 0;
		timeSinceUpdate = 0;
		firstUpdateComplete = false;
		//create the client inline
		JJackAudioProcessor p = new JJackAudioProcessor() {
			private static final long serialVersionUID = 1L;
			@Override
			public synchronized void process(JJackAudioEvent frame) {
				long startTime = System.currentTimeMillis();
				try {
					//grab the buffers
					FloatBuffer[] inBuffer = frame.getInputs();
					FloatBuffer[] outBuffer = frame.getOutputs();
					//turn inputs into float[][] arrays
					for(int i = 0; i < inBuffer.length; i++) {
						inBuffer[i].get(jjackInputs[i]);
					}
					//get the input audio data into the input ring buffer
					for(int i = 0; i < jjBufSize; i++) { //iterate through length of buffer
						for(int j = 0; j < inBuffer.length; j++) { //for each channel
							inputs[j][jjackInputIndex] = jjackInputs[j][i];
						}
						jjackInputIndex++;
						if(jjackInputIndex >= RING_BUFFER_SIZE) {
							jjackInputIndex = 0;
						}
						timeSinceUpdate++;
						//check to see if it's time for another update
						if(timeSinceUpdate >= context.getBufferSize()) {
							doUpdate(); // this propagates update call to context
							//get the generated audio data into the output ring buffer
							for(int i2 = 0; i2 < context.getBufferSize(); i2++) {
								for(int j2 = 0; j2 < context.getAudioFormat().getChannels(); j2++) {
									outputs[j2][beadsOutputIndex] = context.out.getValue(j2, i2);
								}
								beadsOutputIndex++;
								if(beadsOutputIndex >= RING_BUFFER_SIZE) {
									beadsOutputIndex = 0;
								}
							}
							//keep beadsInputIndex updated
							beadsInputIndex += context.getBufferSize();
							beadsInputIndex %= RING_BUFFER_SIZE;
							//reset counter
							timeSinceUpdate = 0;
							//flag that first update is done
							firstUpdateComplete = true;
						}
					}
					//fill temp jjack output array from the output ring buffer
					if(firstUpdateComplete) {
						for(int i = 0; i < jjBufSize; i++) {
							for(int j = 0; j < outBuffer.length; j++) {
								jjackOutputs[j][i] = outputs[j][jjackOutputIndex];
							}
							jjackOutputIndex++;
							if(jjackOutputIndex >= RING_BUFFER_SIZE) {
								jjackOutputIndex = 0;
							}
						}
					}
					//transfer the array data into the outBuffers
					for(int i = 0; i < outBuffer.length; i++) {
						outBuffer[i].put(jjackOutputs[i]);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				System.out.println(System.currentTimeMillis() - startTime);
			}
		};
		//now set up the JJack system
		JJackSystem.setProcessor(p);
		
		//gotta actually have a Thread to keep the system from terminating
		Thread t = new Thread() {
			public void run() {
				while(true) {
					try {
						sleep(100000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		
		return true;
	}
		
	private boolean doUpdate() {
		return super.update();
	}
	
	private class JJackRTInput extends UGen {

		private int[] channels;
		
		public JJackRTInput(AudioContext context, int[] channels) {
			super(context, channels.length);
			this.channels = channels;
		}

		@Override
		public void calculateBuffer() {
			int tempIndex = beadsInputIndex;
			for(int i = 0; i < bufferSize; i++) {
				for(int j = 0; j < channels.length; j++) {
					bufOut[j][i] = inputs[j][tempIndex];
				}
				tempIndex++;
				if(tempIndex >= RING_BUFFER_SIZE) {
					tempIndex = 0;
				}
			}
		}
		
	}

}
