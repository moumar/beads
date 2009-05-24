package net.beadsproject.beads.gui;

import javax.swing.JFrame;

public class BeadsWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	public BeadsPanel content;
	
	public BeadsWindow(String string) {
		super(string);
		content = new BeadsPanel();
		content.horizontalBox();
		content.emptyBorder();
		getContentPane().add(content);
		addKeyListener(BeadsKeys.listener);
	}
}
