package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.play.InterfaceElement;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class LevelMeter implements InterfaceElement {

	private UGen focus;
	
	/** The component. */
	private JComponent component;
	
	private int currentLevel;
	private int numLevels;
	private int range;
	
	public LevelMeter(UGen focus) {
		setFocus(focus);
		setNumLevels(10);
		range = 100;
		currentLevel = 0;
	}

	public UGen getFocus() {
		return focus;
	}

	public void setFocus(UGen focus) {
		this.focus = focus;
	}
	
	public int getNumLevels() {
		return numLevels;
	}

	public void setNumLevels(int numLevels) {
		this.numLevels = Math.max(0, numLevels);
	}

	public JComponent getComponent() {
		if(component == null) {
			component = new BeadsComponent() {
				public void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D)g;
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					int levelBoxHeight = getHeight() / numLevels;
					getCurrentLevel();
					//outer box
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.black);
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					//level
					for(int i = 0; i < currentLevel; i++) {
						if(i < numLevels / 2) {
							g.setColor(Color.green);
						} else if(i < 3 * numLevels / 4f) {
							g.setColor(Color.orange);
						} else {
							g.setColor(Color.red);
						}
						g.fillRect(1, getHeight() - (i + 1) * levelBoxHeight, getWidth() - 2, levelBoxHeight - 1);
					}
				}
			};
			Dimension size = new Dimension(10,100);
			component.setMinimumSize(size);
			component.setPreferredSize(size);
			component.setMaximumSize(size);
			new Thread() {
				public void run() {
					while(true) {
						component.repaint();
						try {
							sleep(100);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();	//TODO this thread never stops!
		}
		return component;
	}
	
	private void getCurrentLevel() {
		if(focus != null && !focus.isPaused() && !focus.isDeleted()) {
			float max = 0f;
			range = Math.min(range, focus.getContext().getBufferSize());
			for(int i = 0; i < range; i++) {
				float val = Math.abs(focus.getValue(0, i));
				if(val > max) max = val;
			}
			currentLevel = (int)(max * numLevels);
		} else {
			currentLevel = 0;
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		AudioContext ac = new AudioContext();
		WavePlayer wp = new WavePlayer(ac, 500f, new SineBuffer().getDefault());
		Gain g = new Gain(ac, 2);
		g.addInput(wp);
		Slider s1 = new Slider(ac, "gain", 0, 1, 1);
		g.setGainEnvelope(s1);
		Slider s2 = new Slider(ac, "freq", 110, 5000, 440);
		wp.setFrequencyEnvelope(s2);
		LevelMeter m = new LevelMeter(g);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.getContentPane().add(s1.getComponent());
		frame.getContentPane().add(s2.getComponent());
		frame.getContentPane().add(m.getComponent());
		frame.pack();
		frame.setVisible(true);
		ac.out.addInput(g);
		ac.start();
	}
	
	
}
