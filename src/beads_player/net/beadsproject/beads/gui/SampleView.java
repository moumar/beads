package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.play.InterfaceElement;

public class SampleView implements InterfaceElement {

	private static Color transparentOverlay = new Color(0.2f, 0.2f, 0.2f, 0.3f);
	public static enum SelectMode {REGION, POSITION};
	
	private Sample sample;
	private int[] view;
	private int selectionStart;
	private int selectionEnd;
	private int height;
	private int width;
	private int chunkSize;
	private JComponent component;
	private SelectMode selectionMode;
	private SampleViewListener listener;

	public SampleView() {
		this(null);
	}

	public SampleView(Sample sample) {
		this.sample = sample;
		height = 100;
		chunkSize = 200;
		setWidth(500);
		selectionMode = SelectMode.REGION;
	}
	
	public int getSelectionStart() {
		return selectionStart;
	}
	
	public void setSelectionStart(int selectionStart) {
		this.selectionStart = selectionStart;
	}
	
	public int getSelectionEnd() {
		return selectionEnd;
	}
	
	public void setSelectionEnd(int selectionEnd) {
		this.selectionEnd = selectionEnd;
	}
	
	public SelectMode getSelectionMode() {
		return selectionMode;
	}
	
	public void setSelectionMode(SelectMode mode) {
		this.selectionMode = mode;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
		calculateOverview();
	}

	public Sample getSample() {
		return sample;
	}

	public void setWidth(int width) {
		this.width = width;
		view = new int[width];
		calculateOverview();
	}
	
	public SampleViewListener getListener() {
		return listener;
	}
	
	public void setListener(SampleViewListener listener) {
		this.listener = listener;
	}

	private void calculateOverview() {
		if(sample != null) {
			float[] frame = new float[sample.getNumChannels()];
			if(sample != null) {
				double hop = (double)sample.getNumFrames() / width;
				for(int i = 0; i < width; i++) {
					int index = (int)(i * hop);
					float average = 0;
					int maxJ = Math.min(chunkSize, (int)sample.getNumFrames() - index);
					for(int j = 0; j < maxJ; j++) {
						sample.getFrame(index + j, frame);
						average += Math.abs(frame[0]);
					}
					if(maxJ != 0) {
						average /= maxJ;
					}
					view[i] = (int)((average + 1f) * (float)height / 2f);
				}
			}
			if(component != null) {
				component.getTopLevelAncestor().repaint();
			}
		}
	}

	public JComponent getComponent() {
		if(component == null) {
			final JComponent subComponent = new JComponent() {
				public void paintComponent(Graphics g) {
//					Graphics2D g2d = (Graphics2D)g;
//					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					//outer box
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.black);
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					//wave
					if(view != null) {
						for(int i = 1; i < view.length; i++) {
							g.drawLine(i - 1, view[i - 1], i, view[i]);
							g.drawLine(i - 1, getHeight() - view[i - 1], i, getHeight() - view[i]);
						}
					}
					//overlay
					g.setColor(transparentOverlay);
					g.fillRect(Math.min(selectionStart, selectionEnd), 0, Math.abs(selectionEnd - selectionStart), height);
				}
			};
			subComponent.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					selectionStart = e.getX();
					selectionEnd = selectionStart + 1;
					if(listener != null) {
						listener.selectionChanged(pixelsToMS(Math.min(selectionStart, selectionEnd)), pixelsToMS(Math.max(selectionStart, selectionEnd)));
					}
					subComponent.repaint();
				}
			});
			subComponent.addMouseMotionListener(new MouseMotionListener() {
				public void mouseDragged(MouseEvent e) {
					switch(selectionMode) {
					case REGION:
						selectionStart = e.getX();
						break;
					case POSITION:
						selectionStart = e.getX();
						selectionEnd = selectionStart + 1;
						break;
					}
					if(listener != null) {
						listener.selectionChanged(pixelsToMS(Math.min(selectionStart, selectionEnd)), pixelsToMS(Math.max(selectionStart, selectionEnd)));
					}
					subComponent.repaint();
				}
				public void mouseMoved(MouseEvent e) {
				}
			});
			component = subComponent;
			Dimension size = new Dimension(width, height);
			subComponent.setMinimumSize(size);
			subComponent.setPreferredSize(size);
			subComponent.setMaximumSize(size);
		}
		return component;
	}
	
	public double pixelsToMS(int pixels) {
		return (double)pixels / (double)width * sample.getLength();
	}

	public int msToPixels(double ms) {
		return (int)(ms * (double)width / sample.getLength());
	}
	
	public interface SampleViewListener {
		public void selectionChanged(double startTimeMS, double endTimeMS);
	}

}
