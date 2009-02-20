package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import net.beadsproject.beads.play.InterfaceElement;


public class Chooser implements InterfaceElement {

	private ArrayList<String> elements;
	private int boxHeight;
	private int boxWidth;
	private int choice;
	private int tempChoice;
	private int textVOffset;
	private ChooserListener listener;
	private JComponent component;
	
	public Chooser() {
		elements = new ArrayList<String>();
		choice = 0;
		boxWidth = 200;
		boxHeight = 10;
		textVOffset = 1;
	}
	
	public JComponent getComponent() {
		component = new JComponent() {
			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				g.drawString(elements.get(choice), 0, boxHeight - textVOffset);
			}
		};
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				final JDialog popup = new JDialog((Frame)component.getTopLevelAncestor());
				popup.setUndecorated(true);
				popup.setModal(true);
				tempChoice = choice;
				final JComponent list = new JComponent() {
					public void paintComponent(Graphics g) {
						Graphics2D g2d = (Graphics2D)g;
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g.setColor(Color.white);
						g.fillRect(0, 0, getWidth(), getHeight());
						g.setColor(Color.black);
						int height = 0;
						for(String s : elements) {
							if(tempChoice == height) {
								g.setColor(Color.gray);
								g.fillRect(0, height * boxHeight, boxWidth, boxHeight);
								g.setColor(Color.black);
							} 
							g.drawString(s, 0, (height++ + 1) * boxHeight - textVOffset);
						}
					}
				};
				list.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						popup.dispose();
						choice = tempChoice;
						component.repaint();
						if(listener != null) {
							listener.choice(elements.get(choice));
						}
					}
				});
				list.addMouseMotionListener(new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
						tempChoice = e.getY() / boxHeight;
						list.repaint();
					}
					public void mouseMoved(MouseEvent e) {
						mouseDragged(e);
					}
				});
				Dimension d = new Dimension(boxWidth, boxHeight * elements.size());
				list.setFont(new Font("Courier", Font.PLAIN, 10));
				list.setMinimumSize(d);
				list.setPreferredSize(d);
				list.setMaximumSize(d);
				popup.setContentPane(list);
				popup.pack();
				popup.setLocation((int)component.getLocationOnScreen().getX(), (int)component.getLocationOnScreen().getY() - choice * boxHeight);
				popup.setVisible(true);
			}
		});
		Dimension size = new Dimension(boxWidth, boxHeight);
		component.setFont(new Font("Courier", Font.PLAIN, 10));
		component.setPreferredSize(size);
		component.setMinimumSize(size);
		component.setMaximumSize(size);
		return component;
	}
	
	public void add(String s) {
		elements.add(s);
	}
		
	public int getBoxHeight() {
		return boxHeight;
	}

	public void setBoxHeight(int boxHeight) {
		this.boxHeight = boxHeight;
	}

	public int getBoxWidth() {
		return boxWidth;
	}

	public void setBoxWidth(int boxWidth) {
		this.boxWidth = boxWidth;
	}

	
	
	public ChooserListener getListener() {
		return listener;
	}

	public void setListener(ChooserListener listener) {
		this.listener = listener;
	}

	public static interface ChooserListener {
		public void choice(String s);
	}
	
	public void setChoice(int choice) {
		this.choice = choice;
		if(listener != null) listener.choice(elements.get(choice));
		if(component != null) component.repaint();
	}

	public static void main(String[] args) {
		Chooser c = new Chooser();
		JFrame f = new JFrame();
		f.add(c.getComponent());
		c.elements.add("Hello");
		c.elements.add("Hffff");
		c.elements.add("xx");
		c.elements.add("xxfff");
		f.pack();
		f.setVisible(true);
	}

}
