package net.beadsproject.beads.liveexample;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.events.IntegerBead;
import net.beadsproject.beads.events.Pattern;
import net.beadsproject.beads.events.PauseTrigger;
import net.beadsproject.beads.events.StopTrigger;
import net.beadsproject.beads.gui.Slider;
import net.beadsproject.beads.play.Environment;
import net.beadsproject.beads.play.SongPart;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;


public class SongPart1 extends SongPart {

	private Buffer wave;
	private Pattern pattern;
	private ArrayList<Hashtable<String, Float>> events;
	
	public SongPart1(String name, Environment environment) {
		super(name, environment, 2);
		pattern = new Pattern();
		events = new ArrayList<Hashtable<String,Float>>();
		wave = new SineBuffer().getDefault();
		Slider s = new Slider(getContext(), "gain", 0f, 1f, 0.5f);
		Gain g = new Gain(getContext(), 2);
//		g.setGainEnvelope(s);
//		addInput(g);
		setGainEnvelope(s);
		interfaceElements.add(s);
		loadPattern();
	}
	
	private void loadPattern() {
		pattern.setLoop(8);
		pattern.setHop(8);
		pattern.addEvent(0, 0);
		pattern.addEvent(2, 1);
		pattern.addEvent(3, 2);
		pattern.addEvent(5, 3);
		Hashtable<String, Float> data = new Hashtable<String, Float>();
		data.put("pitch", 60f);
		data.put("gain", 0.1f);
		data.put("sustain", 100f);
		data.put("decay", 500f);
		events.add(data);
		data = new Hashtable<String, Float>();
		data.put("pitch", 61f);
		data.put("gain", 0.1f);
		data.put("sustain", 500f);
		data.put("decay", 100f);
		events.add(data);
		data = new Hashtable<String, Float>();
		data.put("pitch", 64f);
		data.put("gain", 0.1f);
		data.put("sustain", 100f);
		data.put("decay", 100f);
		events.add(data);
		data = new Hashtable<String, Float>();
		data.put("pitch", 67f);
		data.put("gain", 0.1f);
		data.put("sustain", 1000f);
		data.put("decay", 0f);
		events.add(data);
	}
	
	public void messageReceived(Bead message) {
		int time = ((IntegerBead)message).getInt();
		Integer eventIndex = pattern.getEventAtTime(time);
		if(eventIndex != null) playEvent(events.get(eventIndex));
		if(Math.random() < 0.1) {
			if(Math.random() < 0.5f) {
				pattern.setHop(16);
			} else {
				pattern.setHop(8);
			}
		}
	}
	
	private void playEvent(Hashtable<String, Float> eventData) {
		WavePlayer wp = new WavePlayer(context, Pitch.mtof(eventData.get("pitch")), wave);
		Envelope e = new Envelope(context, eventData.get("gain"));
		Gain g = new Gain(context, 1, e);
		e.addSegment(eventData.get("gain"), eventData.get("sustain"));
		e.addSegment(0f, eventData.get("decay"), new StopTrigger(g));
		g.addInput(wp);
		addInput(g);
	}

	@Override
	public void enter() {
		((Envelope)getGainEnvelope()).lock(false);
		((Envelope)getGainEnvelope()).setValue(0.5f);
	}

	@Override
	public void exit() {
		((Envelope)getGainEnvelope()).addSegment(0f, 5000f, new PauseTrigger(this));
		((Envelope)getGainEnvelope()).lock(true);
	}
	
	
}
