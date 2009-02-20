package net.beadsproject.beads.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;


public class BeadsList extends JList {
	
	DefaultListModel dm;
	
	public BeadsList() {
		dm = new DefaultListModel();
		setModel(dm);
		setFont(new Font("Courier", Font.PLAIN, 10));
		setAlignmentX(LEFT_ALIGNMENT);
		setAlignmentY(TOP_ALIGNMENT);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public void addElement(Object element) {
		dm.addElement(element);
	}

	public Object getLastSelected() {
		Object[] selected = getSelectedValues();
		if(selected.length == 0) {
			return null;
		} else {
			return selected[selected.length - 1];
		}
	}

}
