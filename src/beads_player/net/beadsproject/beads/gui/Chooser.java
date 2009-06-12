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
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.play.InterfaceElement;

public class Chooser implements InterfaceElement {

	protected List<String> elements;
	private int boxHeight;
	private int boxWidth;
	private int charachterWidth;
	private int popupBoxWidth;
	private int choice;
	private int tempChoice;
	private int textVOffset;
	private ChooserListener listener;
	private JComponent component;
	private String label;
	
	public Chooser(String label) {
		setLabel(label);
		elements = new ArrayList<String>();
		choice = 0;
		boxWidth = 100;
		charachterWidth = 5;
		popupBoxWidth = 200;
		boxHeight = 10;
		textVOffset = 1;
	}

	public JComponent getComponent() {
		refreshList();
		final JComponent valueComponent = new JComponent() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
//				Graphics2D g2d = (Graphics2D)g;
//				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				g.drawString(getChoice(), 0, boxHeight - textVOffset);
			}
		};
		valueComponent.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				refreshList();
				final JDialog popup = new JDialog((Frame)component.getTopLevelAncestor());
				popup.setUndecorated(true);
				tempChoice = choice;
				final JComponent list = new JComponent() {
					private static final long serialVersionUID = 1L;
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
								g.fillRect(0, height * boxHeight, popupBoxWidth, boxHeight);
								g.setColor(Color.black);
							} 
							g.drawString(s, 0, (height++ + 1) * boxHeight - textVOffset);
						}
					}
				};
				popup.addWindowFocusListener(new WindowFocusListener() {
					public void windowGainedFocus(WindowEvent e) {
					}
					public void windowLostFocus(WindowEvent e) {
						popup.dispose();
					}
				});
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
				Dimension d = new Dimension(popupBoxWidth, boxHeight * elements.size());
				list.setFont(new Font("Courier", Font.PLAIN, 10));
				list.setMinimumSize(d);
				list.setPreferredSize(d);
				list.setMaximumSize(d);
				popup.setContentPane(list);
				popup.pack();
				popup.setLocation((int)valueComponent.getLocationOnScreen().getX(), (int)valueComponent.getLocationOnScreen().getY() - choice * boxHeight);
				popup.setVisible(true);
			}
		});
		Dimension size = new Dimension(boxWidth, boxHeight);
		valueComponent.setFont(new Font("Courier", Font.PLAIN, 10));
		valueComponent.setPreferredSize(size);
		valueComponent.setMinimumSize(size);
		valueComponent.setMaximumSize(size);
		BeadsPanel container = new BeadsPanel();
		container.horizontalBox();
		String tempLabel = label;
		while(tempLabel.length() < 7) {
			tempLabel = tempLabel + " ";
		}
		JLabel nameLabel = new JLabel(tempLabel);
		nameLabel.setFont(new Font("Courier", Font.PLAIN, 10));
		container.add(nameLabel);
		container.add(valueComponent);
		component = container;
		return component;
	}
	
	public void refreshList() {
		//default is to do nothing, override to change
	}
	
	public void add(String s) {
		elements.add(s);
		int textWidth = s.length() * charachterWidth;
		if(textWidth > popupBoxWidth) {
			popupBoxWidth = textWidth;
		}
		popupBoxWidth = Math.max(200, popupBoxWidth);
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
	
	private void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setChoice(int choice) {
		this.choice = choice;
		if(listener != null) listener.choice(elements.get(choice));
		if(component != null) component.repaint();
	}
	
	public String getChoice() {
		if(elements == null || elements.size() == 0) {
			return "";
		} else {
			if(choice >= elements.size()) {
				choice = elements.size() - 1;
			}
			return elements.get(choice);
		}
	}
	
	public void clear() {
		elements.clear();
	}
	
	public void repaint() {
		if(component != null) {
			component.repaint();
		}
	}
	
	public static Chooser sampleGroupChooser() {
		Chooser c = new Chooser("group") {
			public void refreshList() {
				clear();
				for(String s : SampleManager.groups()) {
					add(s);
				}
				repaint();
			}
		};
		c.refreshList();
		c.setChoice(0);
		return c;
	}

}
