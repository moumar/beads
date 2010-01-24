package net.beadsproject.beads.gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.play.Environment;
import net.beadsproject.beads.play.GroupPlayer;
import net.beadsproject.beads.play.SongGroup;
import net.beadsproject.beads.play.SongPart;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;


public class BeadsGui {
	
	private SongGrid songGrid;
	private BeadsWindow environmentFrame;
	private float temporaryTempo;
	private double timeAtLastTap;
	private Slider tempoSlider;
	private Clock clock;
	
	public BeadsGui() {
		Environment env = null;
		try {
			AudioContext ac = new AudioContext();
			env = Environment.loadEnvironment("net.beadsproject.beads.play.DefaultEnvironmentFactory", ac);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		setup(env);
		timeAtLastTap = 0;
	}
	
	public BeadsGui(Environment env) {
		setup(env);
	}
	
	private void setup(final Environment env) {
		environmentFrame = new BeadsWindow("Beads");
		final JButton audioButton = new JButton("Start") {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				if(env.ac.isRunning()) {
					setText("Stop");
				} else {
					setText("Start");
				}
				super.paintComponent(g);
			}
		};
		audioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(env.ac.isRunning()) {
					env.ac.stop();
				} else {
					env.ac.start();
				}
			}
		});
		audioButton.setFocusable(false);
		environmentFrame.content.add(audioButton);
		EnvironmentPanel environmentPanel = new EnvironmentPanel(env);
		environmentFrame.content.add(environmentPanel);
		GroupPlayer p = new GroupPlayer(env);
		songGrid = new SongGrid(p, environmentPanel);
		songGrid.titledBorder("Song Parts");
		environmentFrame.content.add(songGrid);
		clock = (Clock)env.elements.get("master clock");
		tempoSlider = new Slider(env.ac, "tempo", -200, 1000, 175);
		tempoSlider.storeValue(175f);
		tempoSlider.storeValue(87.5f);
		tempoSlider.storeValue(116.66666f);
		tempoSlider.storeValue(-175f);
		UGen tempoToInterval = new UGen(clock.getContext(), 1, 1) {
			@Override
			public void calculateBuffer() {
				for(int i = 0; i < bufferSize; i++) {
					bufOut[0][i] = 60000f / bufIn[0][i];
				}
			}
		};
		tempoToInterval.addInput(tempoSlider);
		clock.setIntervalEnvelope(tempoToInterval);
		env.ac.out.addDependent(tempoSlider);
		BeadsPanel ci = new BeadsPanel();
		ci.add(tempoSlider.getComponent());
		Slider slider2 = new Slider(env.ac, "gain", 0f, 2f, 1f);
		((Gain)env.ac.out).setGainEnvelope(slider2);
		ci.add(slider2.getComponent());
		ci.titledBorder("Master Controls");
		TimeGraph tg = new TimeGraph(4);
		clock.addMessageListener(tg);
		tg.getComponent().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(clock.isPaused()) {
					clock.reset();
					clock.pause(false);
					if(tempoSlider.getValue() == 0) {
						tempoSlider.setValue(temporaryTempo);
					}
				} else {
					clock.pause(true);
					temporaryTempo = tempoSlider.getCurrentValue();
					tempoSlider.setValue(0f);
				}
			}
		});
		BeadsKeys.addListener(new BeadsKeys.KeyboardListener() {
			public void keyPressed(int keyCode) {
				if(keyCode == KeyEvent.VK_SPACE) {
					if(clock.isPaused()) {
						clock.reset();
						clock.pause(false);
						if(tempoSlider.getValue() == 0) {
							tempoSlider.setValue(temporaryTempo);
						}
					} else {
						clock.pause(true);
						temporaryTempo = tempoSlider.getCurrentValue();
						tempoSlider.setValue(0f);
					}
				} else if(keyCode == KeyEvent.VK_Z) {
					tap();
				} 
			}
			public void keyReleased(int keyCode) {}
		});
		ci.add(tg.getComponent());
		final Readout r = new Readout("time", "");
		clock.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				r.setText(clock.getBeatCount() + " " + clock.getCount());
			}
		});
		ci.add(r.getComponent());
		SampleManagerPanel smp = new SampleManagerPanel(100);
		if(env.elements.containsKey("audioDir")) {
			smp.setRootDir((String)env.elements.get("audioDir"));
		}
		ci.add(smp.getComponent());
		environmentFrame.content.add(ci);
		environmentFrame.setResizable(true);
		environmentFrame.setVisible(true);
		environmentFrame.pack();
		env.ac.start();
	}

	public void repack() {
		((JFrame)songGrid.getTopLevelAncestor()).pack();
	}
	
	
	public void tap() {
		double currentTime = tempoSlider.getContext().getTime();
		double interval = currentTime - timeAtLastTap;
		tempoSlider.setValue((float)(60000.0 / interval));
		timeAtLastTap = currentTime;
//		clock.reset();
	}
	
	public void addSongGroup(SongGroup sg) {
		songGrid.addSongGroup(sg);
		environmentFrame.pack();
	}
	
	public void addSongGroup() {
		songGrid.addSongGroup();
		environmentFrame.pack();
	}
	
	public void addSongGroups(int numGroups) {
		songGrid.addSongGroups(numGroups);
		environmentFrame.pack();
	}
	
	public void setCurrentGroup(int i) {
		songGrid.setCurrentGroup(i);
	}

	public void addSongPart(SongPart sp) {
		songGrid.addSongPart(sp);
		environmentFrame.pack();
	}

}
