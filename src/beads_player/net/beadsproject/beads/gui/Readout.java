package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import net.beadsproject.beads.play.InterfaceElement;

public class Readout implements InterfaceElement {

	private String text;
	private JComponent component;
	private int boxHeight;
	private int boxWidth;
	private int textVOffset;
	
	public Readout() {
		boxHeight = 10;
		boxWidth = 200;
		textVOffset = 1;
	}
	
	public Readout(String text) {
		this();
		setText(text);
	}
	
	public void setText(String text) {
		this.text = text;
		if(component != null) {
			component.repaint();
		}
	}
	
	public String getText() {
		return text;
	}
	
	public JComponent getComponent() {
		if(component == null) {
			component = new JComponent() {
				public void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D)g;
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					if(text != null) {
						g.setColor(Color.black);
						g.drawString(text, 0, boxHeight - textVOffset);
					}
				}
			};
		}
		Dimension size = new Dimension(boxWidth, boxHeight);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		return component;
	}

}
