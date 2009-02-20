package net.beadsproject.beads.gui;

import javax.swing.JFrame;


public class BeadsWindow extends JFrame {

	public BeadsPanel content;
	
	private BeadsWindow() {}
	
	public BeadsWindow(String string) {
		super(string);
		content = new BeadsPanel();
		content.horizontalBox();
		content.emptyBorder();
		getContentPane().add(content);
	}
}
