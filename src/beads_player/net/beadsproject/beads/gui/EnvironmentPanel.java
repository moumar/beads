package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.play.Environment;
import net.beadsproject.beads.play.InterfaceElement;
import net.beadsproject.beads.play.Player;
import net.beadsproject.beads.ugens.Clock;



public class EnvironmentPanel extends BeadsPanel {
	
	private final BeadsList channelsList;
	private final BeadsList pathwaysList;
	protected final BeadsPanel elementsPanel;
	protected final Environment e;
	
	public EnvironmentPanel(Environment e) {
		super();
		this.e = e;
		horizontalBox();
		channelsList = new BeadsList();
		pathwaysList = new BeadsList();
		setupList(channelsList, "Channels");
		setupList(pathwaysList, "Pathways");
		elementsPanel = new BeadsPanel();
		elementsPanel.horizontalBox();
		elementsPanel.emptyBorder();
		elementsPanel.fixSize(100, 200);
		rebuild();
	}
	
	public void setupList(BeadsList list, String name) {
		BeadsPanel panel = new BeadsPanel();
		panel.fixSize(100, 200);
		panel.verticalBox();
		panel.titledBorder(name);
		panel.add(new JScrollPane(list));
		add(panel);
	}
	
	public void rebuild() {
		reloadList(channelsList, e.channels.keySet());
		reloadList(pathwaysList, e.pathways.keySet());
		reloadElementsPanel();
	}
	
	public void reloadList(BeadsList list, Set<String> set) {
		for(String s : set) {
			list.addElement(s);
		}
		list.setSelectedIndex(0);
	}

	public void reloadElementsPanel() {
		for(Object o : e.elements.values()) {
			if(o instanceof InterfaceElement) {
				elementsPanel.add(((InterfaceElement)o).getComponent());
			}
		}
	}
	
	public UGen getSelectedChannel() {
		return e.channels.get((String)channelsList.getLastSelected());
	}

	
	public BeadArray getSelectedPathway() {
		return e.pathways.get((String)pathwaysList.getLastSelected());
	}

	public Clock getSelectedClock() {
		return (Clock)e.elements.get("master clock");	//TODO give this some functionality!
	}
	
}
