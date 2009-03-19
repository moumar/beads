package net.beadsproject.beads.events;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;

public class KeyInput extends Bead implements KeyListener {

	private BeadArray listeners;
	
	public KeyInput() {
		listeners = new BeadArray();
	}
	
	public void addListener(Bead listener) {
		listeners.add(listener);
	}
	
	public void removeListener(Bead listener) {
		listeners.remove(listener);
	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
