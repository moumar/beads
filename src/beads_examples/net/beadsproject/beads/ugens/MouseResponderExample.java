package net.beadsproject.beads.ugens;

import java.applet.Applet;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.MouseResponder;
import net.beadsproject.beads.ugens.WavePlayer;

public class MouseResponderExample extends Applet {

	public void init() {
		main(null);
	}
	
	public static void main(String[] args) {
		//set up the mouse responder
		AudioContext ac = new AudioContext(256, 2000);
		final MouseResponder mouse = new MouseResponder(ac);
		ac.out.addDependent(mouse);
		//set up an envelope type thing which uses mouse.x
		Envelope freqEnvelope = new Envelope(ac) {
			public void calculateBuffer() {
				super.calculateBuffer();
				clear();
				addSegment(mouse.x * 5000f, 20f);
			}
		};
		//set up an envelope type thing which uses mouse.y
		Envelope gainEnvelope = new Envelope(ac) {
			public void calculateBuffer() {
				super.calculateBuffer();
				clear();
				addSegment((1f - mouse.y) * 0.5f, 20f);
			}
		};
		WavePlayer wp = new WavePlayer(ac, 0f, new SineBuffer().getDefault());
		wp.setFrequencyEnvelope(freqEnvelope);
		
		Gain g = new Gain(ac, 1);
		g.setGainEnvelope(gainEnvelope);
		
		g.addInput(wp);
		ac.out.addInput(g);
		ac.start();
	}
	 
}
