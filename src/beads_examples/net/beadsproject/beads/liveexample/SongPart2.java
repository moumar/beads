package net.beadsproject.beads.liveexample;

import java.util.Random;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.buffers.SawBuffer;
import net.beadsproject.beads.events.IntegerBead;
import net.beadsproject.beads.events.PauseTrigger;
import net.beadsproject.beads.events.StopTrigger;
import net.beadsproject.beads.play.Environment;
import net.beadsproject.beads.play.SongPart;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;


public class SongPart2 extends SongPart {

	private Buffer wave;
	Random rng;
	
	public SongPart2(String name, Environment environment) {
		super(name, environment, 2);
		wave = new SawBuffer().getDefault();
		rng = new Random();
		setGainEnvelope(new Envelope(getContext(), 0f));
	}
	
	public void messageReceived(Bead message) {
		if(((IntegerBead)message).getInt() % 8 == 0) {
			WavePlayer wp = new WavePlayer(context, Pitch.mtof(rng.nextInt(127)), wave);
			Envelope e = new Envelope(context, 0.1f);
			Gain g = new Gain(context, 1, e);
			e.addSegment(0f, 100f, new StopTrigger(g));
			g.addInput(wp);
			addInput(g);
		}
	}

	@Override
	public void enter() {
		((Envelope)getGainEnvelope()).setValue(0.5f);
	}

	@Override
	public void exit() {
		((Envelope)getGainEnvelope()).addSegment(0f, 5000f, new PauseTrigger(this));
	}
	
	
	
}
