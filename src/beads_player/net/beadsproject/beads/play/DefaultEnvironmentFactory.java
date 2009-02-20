package net.beadsproject.beads.play;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.MouseResponder;
import net.beadsproject.beads.ugens.RTInput;

public class DefaultEnvironmentFactory extends EnvironmentFactory {

	public Environment createEnvironment() {
		Environment e = new Environment();
		e.ac = new AudioContext(512, 5000);
		//set up clock as a pathway
		Clock c = new Clock(e.ac, 500f);
		c.setName("master clock");
		e.ac.out.addDependent(c);
		BeadArray clockListener = new BeadArray();
		c.addMessageListener(clockListener);
		e.pathways.put(c.getName(), clockListener);
		//and also as an object
		e.elements.put(c.getName(), c);
		//set up mouse as object in environment
		MouseResponder mouse = new MouseResponder(e.ac);
		e.ac.out.addDependent(mouse);
		e.elements.put("mouse", mouse);
		//set up audio input as object in environment
		RTInput in = new RTInput(e.ac, e.ac.getAudioFormat());
		e.elements.put("in", in);
		//set up in and main out as object in channels
		e.channels.put("out", e.ac.out);
//		//make some other simple UGen to add
//		e.channels.put("dummy", new Gain(e.ac, 2));
		return e;
	}
}
