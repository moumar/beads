package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import net.beadsproject.beads.analysis.SegmentListener;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.play.InterfaceElement;

public class OnsetView extends UGen implements InterfaceElement, SegmentListener {

	int framesSinceOnset;
	JComponent component = null;
	
	public OnsetView(AudioContext context) {
		super(context);
		framesSinceOnset = 20;
	}

	public JComponent getComponent() {
		if(component == null) {
			class OnsetViewComponent extends BeadsComponent {
				private static final long serialVersionUID = 1L;
				public void paintComponent(Graphics g) {
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.black);
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					float intensity = Math.max(0, 1f - (float)framesSinceOnset / 20f);
					g.setColor(new Color(0.2f, 0.1f, 0f, intensity));
					g.fillRect(10, 10, getWidth() - 20, getHeight() - 20);
				}
			};
			component = new OnsetViewComponent(); 
			Dimension size = new Dimension(100, 100);
			component.setMinimumSize(size);
			component.setPreferredSize(size);
			component.setMaximumSize(size);
		}
		return component;
	}

	public void newSegment(TimeStamp start, TimeStamp end) {
		framesSinceOnset = 0;
	}

	@Override
	public void calculateBuffer() {
		framesSinceOnset++;
		if(component != null && framesSinceOnset % 5 == 0) {
			component.repaint();
		}
	}

}
