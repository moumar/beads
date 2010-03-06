package net.beadsproject.beads.ugens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer.LoopType;


public class GranularSamplePlayerExample2 {

	public static void main(String[] args) {
		JFrame f = new JFrame();
		JPanel p = new JPanel();
		f.setContentPane(p);
		
		AudioContext ac = new AudioContext();
    	Sample s = SampleManager.sample("audio/1234.aif");	
		final GranularSamplePlayer gsp = new GranularSamplePlayer(ac, s);
		gsp.setLoopType(LoopType.LOOP_ALTERNATING);
		gsp.getLoopStartEnvelope().setValue(0f);
		gsp.getLoopEndEnvelope().setValue(1000f);
		gsp.getRandomPanEnvelope().setValue(1f);
		ac.out.addInput(gsp);
		gsp.pause(true);
		
		JButton pauseButton = new JButton();
		p.add(pauseButton);
		pauseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				gsp.pause(!gsp.isPaused());
			}
			
		});
		
		f.pack();
		f.setVisible(true);
		ac.start();
	}
}
