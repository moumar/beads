package net.beadsproject.beads.data;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.RTInput;
import net.beadsproject.beads.ugens.Recorder;

/**
 * Record audio from microphone, then output it into a file.
 * Demonstrates use of Recording an unknown amount of audio.
 * @author ben
 */
public class RecordingSessionExample {

	public static void main(String[] args) throws Exception {
		final AudioContext ac = new AudioContext();
		
		// read input from microphone
		RTInput input = new RTInput(ac);
		  
		// set up a recorder
		final Sample s = new Sample(ac.getAudioFormat(),1000);
		final Recorder r = new Recorder(ac,s,Recorder.Mode.INFINITE);
		r.addInput(input);
		
		// ADVANCED (and demonstratively ONLY) usage of recorder resizing parameters.
		// Don't double the sample size, simply resize by 1 minute chunks at a time
		r.setResizingParameters(0, 1*60*1000);
		
		// set up a user-input trigger
		final WindowListener trigger = new WindowListener()
		{
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				// stop the recorder, and clip the final sample
				ac.stop();
				r.pause(true);
				// r.clip(); (don't clip, let's here the silence too....)
				
				// output the sample to a file
				try {
					s.write("audio/recordingsession.aif");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// stop the program
				ac.stop();
			}

			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		};
		
		JFrame frame = new JFrame("Recording Session Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel("recording... close to quit."));
        frame.addWindowListener(trigger);
        frame.pack();
        frame.setVisible(true);
        
		// 
		ac.out.addDependent(r);
		ac.start();
	}

}
