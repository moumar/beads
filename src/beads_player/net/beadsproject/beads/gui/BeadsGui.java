package net.beadsproject.beads.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.play.Environment;
import net.beadsproject.beads.play.Player;
import net.beadsproject.beads.play.SongGroup;
import net.beadsproject.beads.play.SongPart;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;


public class BeadsGui {
	
	SongGrid songGrid;
	
	public BeadsGui() {
		Environment env = null;
		try {
			env = Environment.loadEnvironment("com.olliebown.beads.play.DefaultEnvironmentFactory");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		setup(env);
	}
	
	public BeadsGui(Environment env) {
		setup(env);
	}
	
	private void setup(final Environment env) {
		BeadsWindow environmentFrame = new BeadsWindow("Beads");
		final JButton audioButton = new JButton("Start");
		audioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(env.ac.isRunning()) {
					env.ac.stop();
					audioButton.setText("Start");
				} else {
					env.ac.start();
					audioButton.setText("Stop");
				}
			}
		});
		environmentFrame.content.add(audioButton);
		EnvironmentPanel environmentPanel = new EnvironmentPanel(env);
		environmentFrame.content.add(environmentPanel);
		Player p = new Player(env);
		songGrid = new SongGrid(p, environmentPanel);
		songGrid.titledBorder("Song Parts");
		environmentFrame.content.add(songGrid);
		Clock clock = (Clock)env.elements.get("master clock");
		Slider slider = new Slider(env.ac, "tempo", 5, 500, 175);
		UGen tempoToInterval = new UGen(clock.getContext(), 1, 1) {
			@Override
			public void calculateBuffer() {
				for(int i = 0; i < bufferSize; i++) {
					bufOut[0][i] = 60000f / bufIn[0][i];
				}
			}
		};
		tempoToInterval.addInput(slider);
		clock.setIntervalEnvelope(tempoToInterval);
		BeadsPanel ci = new BeadsPanel();
		ci.add(slider.getComponent());
		environmentFrame.content.add(ci);
		slider = new Slider(env.ac, "gain", 0f, 1f, 1f);
		((Gain)env.ac.out).setGainEnvelope(slider);
		ci.add(slider.getComponent());
		ci.titledBorder("Master Controls");
		environmentFrame.content.add(ci);
		environmentFrame.setResizable(true);
		environmentFrame.setVisible(true);
		environmentFrame.pack();
	}

	public void repack() {
		((JFrame)songGrid.getTopLevelAncestor()).pack();
	}
	
	public synchronized void addSongGroup(SongGroup sg) {
		songGrid.addSongGroup(sg);
		repack();
	}

	public synchronized void addSongPart(SongPart sp) {
		songGrid.addSongPart(sp);
		repack();
	}

}
