package net.beadsproject.beads.core.io;
import net.beadsproject.beads.core.AudioContext;
import jvst.wrapper.VSTPluginAdapter;


public abstract class BeadsPlugin extends VSTPluginAdapter {

	JVSTAudioIO jVSTAudioIO;
	
	public BeadsPlugin(long Wrapper) {
		super(Wrapper);
		jVSTAudioIO = new JVSTAudioIO();
		AudioContext ac = new AudioContext(jVSTAudioIO);
		setup(ac);
	}
	
	public abstract void setup(AudioContext ac);

	@Override
	public void processReplacing(float[][] in, float[][] out, int length) {
		jVSTAudioIO.process(in, out);
	}

}
