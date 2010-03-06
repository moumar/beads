package net.beadsproject.beads.data;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample.TotalRegime;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Recorder;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * Demonstrates writing into a sample then writing to disk.
 * 
 * @author ben
 */
public class WritingToDiskExample {

	public static void main(String[] args) throws Exception {
		final AudioContext ac = new AudioContext();
		
		// set up something to record
		Envelope e = new Envelope(ac,440f);
		e.addSegment(220f, 50f);
		e.addSegment(110f, 50f);
		e.addSegment(440f, 2000f);		
		WavePlayer wp = new WavePlayer(ac,e,new SineBuffer().getDefault());
		
		// Load a sample and overwrite the first part of it
		TotalRegime tr = new Sample.TotalRegime();
		tr.storeInNativeBitDepth = true; // native bit depth = false will work too...
		final Sample s = new Sample("audio/1234.aif",tr);
		final Recorder r = new Recorder(ac,s);
		r.addInput(wp);
		
		// once the recording is finished
		// dump the sample to a file
		// and kill the ac
		DelayTrigger dt = new DelayTrigger(ac,2000f,new Bead()
		{
			public void messageReceived(Bead b)
			{
				r.pause(true);
				
				try {
					s.write("audio/writingToDiskNBD.aif");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ac.stop();
			}
		});
		
		ac.out.addDependent(dt);
		
		// record, and also play 
		ac.out.addDependent(r);
		ac.out.addInput(wp);
		ac.start();
	}

}
