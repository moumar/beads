package net.beadsproject.beads.play;

import java.util.Random;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.events.StopTrigger;
import net.beadsproject.beads.gui.BeadsWindow;
import net.beadsproject.beads.play.DefaultEnvironmentFactory;
import net.beadsproject.beads.play.Environment;
import net.beadsproject.beads.play.SongPart;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;


public class SongPartExample {
	
	public static void main(String[] args) {
		//set it up
		Environment e = new DefaultEnvironmentFactory().createEnvironment();
		SongPart sp = new MySongPart("track1", e);
		e.ac.out.addInput(sp);
		e.pathways.get("master clock").add(sp);
		e.ac.start();
		//look at it
		BeadsWindow bw = new BeadsWindow("Song Part Example");
		bw.getContentPane().add(sp.getComponent());
		bw.pack();
		bw.setVisible(true);
		//play it
		sp.pause(false);
	}
	
	public static class MySongPart extends SongPart {

		private Buffer wave;
		Random rng;
		
		public MySongPart(String name, Environment environment) {
			super(name, environment, 2);
			wave = new SineBuffer().getDefault();
			rng = new Random();
		}
		
		public void messageReceived(Bead message) {
			System.out.println("message received");
			WavePlayer wp = new WavePlayer(context, Pitch.mtof(rng.nextInt(127)), wave);
			Envelope e = new Envelope(context, 0.1f);
			Gain g = new Gain(context, 1, e);
			e.addSegment(0f, 100f, new StopTrigger(g));
			g.addInput(wp);
			addInput(g);
		}

		@Override
		public void enter() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void exit() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
