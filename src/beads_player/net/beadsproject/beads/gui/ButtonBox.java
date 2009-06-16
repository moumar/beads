package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import net.beadsproject.beads.play.InterfaceElement;


public class ButtonBox implements InterfaceElement {

	public static enum SelectionMode {SINGLE_SELECTION, MULTIPLE_SELECTION};
	
	private int boxWidth;
	private boolean[][] buttons;
	private ButtonBoxListener listener;
	private int previousX = -1, previousY = -1;
	private SelectionMode selectionMode;
	
	public ButtonBox(int width, int height) {
		this(width, height, SelectionMode.SINGLE_SELECTION);		
	}
	
	public ButtonBox(int width, int height, SelectionMode selectionMode) {
		buttons = new boolean[width][height];
		boxWidth = 10;
		this.selectionMode = selectionMode;	
	}
	
	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

	public JComponent getComponent() {
		final JComponent component = new JComponent() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				//outer box
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				for(int i = 0; i < buttons.length; i++) {
					for(int j = 0; j < buttons[i].length; j++) {
						if(buttons[i][j]) {
							g.setColor(Color.black);
							g.fillRect(i * boxWidth, j * boxWidth, boxWidth, boxWidth);
						} else {
							g.setColor(Color.gray);
							g.drawRect(i * boxWidth, j * boxWidth, boxWidth, boxWidth);
						}
					}
				}
				g.setColor(Color.gray);
				g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
				g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
			}
		};
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int i = e.getX() / boxWidth;
				int j = e.getY() / boxWidth;
				if(i >= 0 && i < buttons.length && j >= 0 && j < buttons[0].length) {
					makeSelection(i, j);
					component.repaint();
				}
			}
		});
		component.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				int i = e.getX() / boxWidth;
				int j = e.getY() / boxWidth;
				if(i >= 0 && i < buttons.length && j >= 0 && j < buttons[0].length) {
					if(i != previousX || j != previousY) {
						makeSelection(i, j);
						component.repaint();
					}
				}
			}
			public void mouseMoved(MouseEvent e) {}
		});
		Dimension size = new Dimension(buttons.length * boxWidth + 1, buttons[0].length * boxWidth + 1);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		return component;
	}
	
	public void makeSelection(int i, int j) {
		switch(selectionMode) {
		case SINGLE_SELECTION:
			boolean currentValue = buttons[i][j];
			if(previousX != -1) {
				buttons[previousX][previousY] = false;
			}
			buttons[i][j] = !currentValue;
			if(listener != null) {
				if(buttons[i][j]) {
					if(previousX != i && previousY != j && previousX != -1) {
						listener.buttonOff(previousX, previousY);
					}
					listener.buttonOn(i, j);
				} else {
					listener.buttonOff(i, j);
				}
			}
			if(buttons[i][j] == false) {
				previousX = -1;
				previousY = -1;
			} else {
				previousX = i;
				previousY = j;
			}
			break;
		case MULTIPLE_SELECTION:
			buttons[i][j] = !buttons[i][j];
			if(listener != null) {
				if(buttons[i][j]) {
					listener.buttonOn(i, j);
				} else {
					listener.buttonOff(i, j);
				}
			}
			previousX = i;
			previousY = j;
			break;
		}
	}

	public static interface ButtonBoxListener {
		public void buttonOn(int i, int j);
		public void buttonOff(int i, int j);
	}

	public int getBoxWidth() {
		return boxWidth;
	}

	public void setBoxWidth(int boxWidth) {
		this.boxWidth = boxWidth;
	}

	public ButtonBoxListener getListener() {
		return listener;
	}

	public void setListener(ButtonBoxListener listener) {
		this.listener = listener;
	}

	public boolean[][] getButtons() {
		return buttons;
	}
	
	public int getWidth() {
		return buttons.length;
	}
	
	public int getHeight() {
		return buttons[0].length;
	}
	
}
