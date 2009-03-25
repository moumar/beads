package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.play.InterfaceElement;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;


//TODO -- a far superior Slider2D would work with any 2 UGens as X and Y inputs, but be able to
//override them somehow -- perhaps could flip between input mode and manual mode
//alternatives could have various elastic or oscillatory behaviour, and be clock-aware.
public class Slider2D implements InterfaceElement {

	private final Slider sliderX;
	private final Slider sliderY;
	private JComponent component;
	
	public Slider2D(Slider sliderX, Slider sliderY) {
		this.sliderX = sliderX;
		this.sliderY = sliderY;
		
	}

	public JComponent getComponent() {
		if(component == null) {
			component = new BeadsComponent() {
				public void paintComponent(Graphics g) {
//					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					//outer box
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.gray);
					g.drawString(sliderX.getName(), 2, 12);
					g.drawString("" + sliderX.getValue(), 2, 26);
					g.drawString(sliderY.getName(), 2, 40);
					g.drawString("" + sliderY.getValue(), 2, 54);
					g.setColor(Color.black);
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					g.setColor(new Color(0f, 0f, 0f, 0.5f));
					int xpos = (int)(sliderX.getValueFract() * getWidth());
					int ypos = (int)((1f - sliderY.getValueFract()) * getHeight());
					g.fillOval(xpos - 5, ypos - 5, 10, 10);
				}
			};
			component.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					sliderX.setValueFract((float)e.getX() / (float)component.getHeight());
					sliderY.setValueFract(1f - (float)e.getY() / (float)component.getHeight());
					component.repaint();
				}
			});
			component.addMouseMotionListener(new MouseMotionListener() {
				public void mouseDragged(MouseEvent e) {
					sliderX.setValueFract((float)e.getX() / (float)component.getHeight());
					sliderY.setValueFract(1f - (float)e.getY() / (float)component.getHeight());
					component.repaint();
				}
				public void mouseMoved(MouseEvent e) {
				}
			});
			component.setMinimumSize(new Dimension(100,100));
			component.setPreferredSize(new Dimension(100,100));
			component.setMaximumSize(new Dimension(100,100));
		}
		return component;
	}

	public Slider getSliderX() {
		return sliderX;
	}

	public Slider getSliderY() {
		return sliderY;
	}
	

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		AudioContext ac = new AudioContext();
		
		for(int i = 0; i < 2; i++) {
			WavePlayer wp = new WavePlayer(ac, 500f, new SineBuffer().getDefault());
			Gain g = new Gain(ac, 2);
			g.addInput(wp);
			Slider s1 = new Slider(ac, "gain", 0, 1, 0.5f);
			g.setGainEnvelope(s1);
			Slider s2 = new Slider(ac, "freq", 110, 5000, 440);
			wp.setFrequencyEnvelope(s2);
			Slider2D s2d = new Slider2D(s2, s1);
			frame.getContentPane().add(s2d.getComponent());
			ac.out.addInput(g);
		}
		
		frame.pack();
		frame.setVisible(true);
		ac.start();
	}
	
}
