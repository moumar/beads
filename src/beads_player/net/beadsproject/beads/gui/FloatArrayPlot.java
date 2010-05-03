package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.analysis.SegmentListener;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.play.InterfaceElement;

public class FloatArrayPlot implements InterfaceElement, SegmentListener {

	JComponent component;
	float[] data;
	float min, max, range;
	boolean adaptiveRange;
	FeatureExtractor<float[], ?> extractor;
	
	public FloatArrayPlot(FeatureExtractor<float[], ?> extractor, float min, float max) {
		this(min, max);
		listenTo(extractor);
	}

	public FloatArrayPlot(FeatureExtractor<float[], ?> extractor, boolean adaptive) {
		this(adaptive);
		listenTo(extractor);
	}
	
	public FloatArrayPlot(float min, float max) {
		this.min = min;
		this.max = max;
		range = max - min;
		adaptiveRange = false;
	}
	
	public FloatArrayPlot(boolean adaptive) {
		min = 0;
		max = 0;
		range = 0;
		adaptiveRange = adaptive;
	}
	
	public JComponent getComponent() {
		if(component == null) {
			component = new BeadsComponent() {
				private static final long serialVersionUID = 1L;
				public void paintComponent(Graphics g) {
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.black);
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					if(data != null && range != 0) {
						float blockWidth = (float)getWidth() / data.length;
						g.setColor(Color.lightGray);
						for(int i = 0; i < data.length; i++) {
							int height = (int)((data[i] - min) / range * getHeight());
							g.fillRect((int)(i * blockWidth), getHeight() - height, Math.max(3, (int)blockWidth), height);
						}
					}
				}
			};
		}
		Dimension size = new Dimension(200, 100);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		return component;
	}
	
	public void listenTo(FeatureExtractor<float[], ?> extractor) {
		this.extractor = extractor;
	}

	public void newSegment(TimeStamp start, TimeStamp end) {
		if(component != null && extractor != null) {
			data = extractor.getFeatures();
			if(adaptiveRange && data != null) {
				for(int i = 0; i < data.length; i++) {
					if(min > data[i]) min = data[i];
					if(max < data[i]) max = data[i];
				}
				range = max - min;
			}
			component.repaint();
		}
	}

}
