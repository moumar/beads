package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.play.InterfaceElement;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;



// TODO: Auto-generated Javadoc
/**
 * The Class Slider.
 * 
 * @author ollie
 * 
 *         A UGen which provides a GUI slider for audio control. incomplete
 */
public class Slider extends Envelope implements InterfaceElement {

	/** The min. */
	private float min;
	
	/** The max. */
	private float max;
	
	/** The value. */
	private float value;
	
	/** The component. */
	private JComponent component;
	
	private float smoothnessInterval;
	
	private String name;
	
	/**
	 * Instantiates a new slider.
	 * 
	 * @param context
	 *            the context
	 * @param name
	 *            the name
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param value
	 *            the value
	 */
	public Slider(AudioContext context, String nam, float min, float max, float val) {
		//TODO error check max and min values
		super(context);
		name = nam;
		setMin(min);
		setMax(max);
		setValue(val);
		smoothnessInterval = 20;
	}
	
	public float calculateValueFromFract(float fract) {
		return fract * (max - min) + min;
	}
	
	public void setValueFract(float fract) {
		setValue(fract * (max - min) + min);
	}
	
	public float getValueFract() {
		return (value - min) / (max - min);
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(float val) {
		this.value = Math.max(min, Math.min(max, val));
		clear();
		addSegment(value, smoothnessInterval);
		if(component != null) component.repaint();
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#getValue()
	 */
	public float getValue() {
		return value;
	}
	
	/**
	 * Gets the min.
	 * 
	 * @return the min
	 */
	public float getMin() {
		return min;
	}
	
	/**
	 * Sets the min.
	 * 
	 * @param min
	 *            the new min
	 */
	public void setMin(float min) {
		this.min = min;
	}
	
	/**
	 * Gets the max.
	 * 
	 * @return the max
	 */
	public float getMax() {
		return max;
	}
	
	/**
	 * Sets the max.
	 * 
	 * @param max
	 *            the new max
	 */
	public void setMax(float max) {
		this.max = max;
	}
	
	

	public float getSmoothnessInterval() {
		return smoothnessInterval;
	}

	public void setSmoothnessInterval(float smoothnessInterval) {
		this.smoothnessInterval = smoothnessInterval;
	}

	public void calculateBuffer() {
		super.calculateBuffer();
		value = bufOut[0][0];
//		if(component != null) component.repaint();
	}
	
	/**
	 * Gets the panel.
	 * 
	 * @return the panel
	 */
	public JComponent getComponent() {
		if(component == null) {
			component = new BeadsComponent() {
				public void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D)g;
//					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					//outer box
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.gray);
					g2d.rotate(-Math.PI / 2f);
					g.drawString(name, -getHeight() + 2, 12);
					g.drawString("" + value, -getHeight() + 2, 26);
					g2d.rotate(Math.PI / 2f);
					g.setColor(Color.black);
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					//value fract
					int sliderHeight = (int)(getHeight() * (1f - getValueFract()));
					if(isLocked()) {
						g.setColor(Color.gray);
					}
					g.drawLine(0, sliderHeight, getWidth(), sliderHeight);
				}
			};
			component.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if((e.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
						runEnvelopeDrawPanel();
					} else {
						if(!isLocked()) {
							setValueFract(1f - (float)e.getY() / (float)component.getHeight());
						}
					}
				}
			});
			component.addMouseMotionListener(new MouseMotionListener() {
				public void mouseDragged(MouseEvent e) {
					if(!isLocked()) {
						setValueFract(1f - (float)e.getY() / (float)component.getHeight());
					}
				}
				public void mouseMoved(MouseEvent e) {
				}
			});
			Dimension size = new Dimension(30,100);
			component.setMinimumSize(size);
			component.setPreferredSize(size);
			component.setMaximumSize(size);
		}
		return component;
	}

	private void runEnvelopeDrawPanel() {
		final int[] x = new int[2];
		x[0] = x[1] = 0;
		final int[] y = new int[2];
		y[0] = y[1] = (int)((1f - getValueFract()) * component.getHeight());
		final JPanel drawPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				g.drawLine(x[0], y[0], x[1], y[1]);
			}
		};
		final JDialog drawWindow = new JDialog((Frame)component.getTopLevelAncestor());
		drawWindow.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
			}
			public void mouseMoved(MouseEvent e) {
				x[1] = e.getX();
				y[1] = e.getY();
				drawPanel.repaint();
			}
		});
		drawWindow.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				drawWindow.dispose();
				setValue(calculateValueFromFract(1f - y[0] / (float)component.getHeight()));
				addSegment(calculateValueFromFract(1f - y[1] / (float)component.getHeight()), x[1] / (float)drawPanel.getWidth() * 10000f);
			}
		});
		drawWindow.setContentPane(drawPanel);
		drawWindow.setUndecorated(true);
		drawWindow.setModal(true);
		drawWindow.setSize(new Dimension(component.getWidth() + 100, component.getHeight()));
		drawWindow.setLocation(new Point(component.getLocationOnScreen().x + component.getWidth(), component.getLocationOnScreen().y));
		drawWindow.setVisible(true);
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
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.getContentPane().add(s1.getComponent());
		frame.getContentPane().add(s2.getComponent());
		frame.pack();
		frame.setVisible(true);
		ac.out.addInput(g);
		ac.start();
		
	}
	
}
