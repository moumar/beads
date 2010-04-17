package net.beadsproject.beads.core.io;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.UGen;

public class JVSTAudioIO extends AudioIO {

	private float[][] inputs;
	
	@Override
	protected UGen getAudioInput(int[] channels) {
		return new JVSTAudioInput(context, channels);
	}

	@Override
	protected boolean start() {
		return true;
	}

	protected void process(float[][] in, float[][] out) {
		//(1) store the inputs for access
		this.inputs = in;
		//(2) update
		update();
		//(3) sent outs 
		//(need to test this, might be necessary to copy instead of pass)
		for(int i = 0; i < out.length; i++) {
				out[i] = context.out.getOutBuffer(i); 
		}
	}
	
	private class JVSTAudioInput extends UGen {

		private int[] channels;
		
		public JVSTAudioInput(AudioContext context, int[] channels) {
			super(context, channels.length);
			this.channels = channels;
			outputInitializationRegime = OutputInitializationRegime.NULL;
			outputPauseRegime = OutputPauseRegime.ZERO;
		}

		@Override
		public void calculateBuffer() {
			for(int i = 0; i < outs; i++) {
				bufOut[i] = inputs[channels[i]];
			}
		}
		
	}

}
