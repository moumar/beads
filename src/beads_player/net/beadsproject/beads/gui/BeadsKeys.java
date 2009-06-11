package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class BeadsKeys {

	public final static KeyListener listener = new KeyListener() {
		
		public void keyPressed(KeyEvent e) {
			keysDown.add((Integer)e.getKeyCode());
		}
	
		public void keyReleased(KeyEvent e) {
			keysDown.remove((Integer)e.getKeyCode());
		}
	
		public void keyTyped(KeyEvent e) {
			//do nothing
		}
	};
	
	private final static ArrayList<Integer> keysDown = new ArrayList<Integer>();
	
	public static boolean keyDown(int keyCode) {
		return keysDown.contains(keyCode);
	}
	
	public static ArrayList<Integer> keysDown() {
		return (ArrayList<Integer>)keysDown.clone();
	}

	public static void printState() {
		for(Integer i : keysDown) {
			System.out.println(i);
		}
	}
	
	public static void main(String[] args) {
		final JFrame f = new JFrame();
		f.addKeyListener(BeadsKeys.listener);
		JComponent c = new JComponent() {
			{
			setBackground(Color.black);
			}
			public void paintComponent(Graphics g) {
				g.clearRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.white);
				int count = 0;
				for(Integer i : keysDown()) {
					g.drawString("" + i, count++ * 20, 20);
				}
			}
		};
		f.add(c);
		f.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				f.repaint();
			}
			public void keyReleased(KeyEvent e) {
				f.repaint();
			}
			public void keyTyped(KeyEvent e) {
				f.repaint();
			}
		});
		f.setVisible(true);
	}
	
}
