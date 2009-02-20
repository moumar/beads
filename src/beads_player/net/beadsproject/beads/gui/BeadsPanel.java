package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class BeadsPanel extends JPanel {
	
	private int spacing = 1;
	
	public BeadsPanel() {	
		setAlignmentX(LEFT_ALIGNMENT);
		setAlignmentY(TOP_ALIGNMENT);
		setFont(new Font("Courier", Font.PLAIN, 10));
	}
	
	public void horizontalBox() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
	
	public void verticalBox() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	public void lineBorder() {
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
	}
	
	public void emptyBorder() {
		setBorder(BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing));
	}
	
	public void lineEmptyBorder() {
		Border line = BorderFactory.createLineBorder(Color.black, 1);
		Border empty = BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing);
		Border compound = BorderFactory.createCompoundBorder(
				line, empty);
		setBorder(compound);
	}
	
	public void titledBorder(String title) {
		Border empty = BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing);
		Border titleBorder = BorderFactory.createTitledBorder(empty, title, 
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, 
				new Font("Courier", Font.BOLD, 11));
		setBorder(titleBorder);
	}
	
	public void highlight() {
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.red), getBorder()));
	}
	
	public void addSpace() {
		Component space = Box.createRigidArea(new Dimension(spacing, spacing));
		space.setBackground(Color.white);
		add(space);
	}
	
	public void addVerticalSeparator() {
		addSpace();
		add(new JSeparator(SwingConstants.VERTICAL));
		addSpace();
	}
	
	public void addHorizontalSeparator() {
		addSpace();
		add(new JSeparator(SwingConstants.HORIZONTAL));
		addSpace();
	}

	public void fixSize(int i, int j) {
		Dimension d = new Dimension(i, j);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
	}
	
}
