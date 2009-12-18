package net.beadsproject.beads.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.events.Pattern;
import net.beadsproject.beads.gui.SingleButton.Mode;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * Interface for a Pattern object. Can save and load.
 */
public class PatternGrid extends ButtonBox {

	private Pattern pattern;
	private JComponent component;
	
	public PatternGrid(int width, int height) {
		super(width, height, SelectionMode.MULTIPLE_SELECTION);
		pattern = new Pattern();
		pattern.setLoop(width);
		setListener(new ButtonBoxListener() {
			public void buttonOff(int i, int j) {
//				pattern.removeEvent(i * pattern.getHop(), j);
				pattern.removeEvent(i, j);
			}
			public void buttonOn(int i, int j) {
//				pattern.addEvent(i * pattern.getHop(), j);
				pattern.addEvent(i, j);
			}
		});
	}
	
	public void setPattern(Pattern pattern) {
		int numNotesVisible = 20;
		this.pattern = pattern;
		resize(pattern.getLoop(), numNotesVisible);
		setBoxWidth(200f / pattern.getLoop());
		setBoxHeight(100f / numNotesVisible);
		setBBFromPattern();
		if(component != null) component.repaint();
	}
	
	private void setBBFromPattern() {
		clear();
		for(Integer i : pattern.getEvents()) {
			if(i < buttons.length) {
				ArrayList<Integer> sounds = pattern.getEventAtIndex(i);
				if(sounds != null) {
					for(Integer j : sounds) {
						if(j < buttons[i].length) {
							buttons[i][j] = true;
						}
					}
				}
			}
		}
	}
	
	public ArrayList<Integer> goToStep(int index) {
		//work out the column to highlight
		ArrayList<Integer> event = pattern.getEventAtStep(index);
		setColumnHighlight(pattern.getLastIndex());	//does this break if the continuous update mode changes?
		//the return the data
		return event;
	}
	
	public JComponent getComponent() {
		if(component == null) {
			JComponent bb = super.getComponent();
			final BeadsPanel panel = new BeadsPanel();
			//might want to make Overlay an option...
//			OverlayLayout ol = new OverlayLayout(panel);
//			panel.setLayout(ol);
			BeadsPanel overlay = new BeadsPanel() {
				public void paintComponent(Graphics g) {
					g.setColor(Color.gray);
					g.fillRect(0,0,getWidth(),getHeight());
				}
			};
			overlay.fixSize(bb.getPreferredSize());
			//TODO this didn't work out, try again later
//			panel.add(overlay);
			panel.add(bb);
			
			
			
			SingleButton sb = new SingleButton("Read", Mode.ONESHOT);
			sb.setListener(new SingleButton.SingleButtonListener() {
				public void buttonPressed(boolean newState) {
					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showOpenDialog(panel);
			        if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							read(chooser.getSelectedFile().getAbsolutePath());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
			        } 
				}
			});
			panel.add(sb.getComponent());
			sb = new SingleButton("Write", Mode.ONESHOT);
			sb.setListener(new SingleButton.SingleButtonListener() {
				public void buttonPressed(boolean newState) {
					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showSaveDialog(panel);
			        if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							write(chooser.getSelectedFile().getAbsolutePath());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
			        }
				}
			});
			panel.add(sb.getComponent());

			component = panel;
		}
		return component;
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public void read(String filename) {
		URL url = AudioUtils.urlFromString(filename);
		if(url == null) {
			return;
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(url.openStream());
			pattern = (Pattern)ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setBBFromPattern();
		if(component != null) component.repaint();
	}
	
	public void write(String filename) {
		File f = new File(filename);
		try {
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(pattern);
			oos.close();
			fos.close();
		} catch(Exception e) {
			//suppress
		}
	}
	
	public static void main(String[] args) {

		final AudioContext ac = new AudioContext();
		final Clock clock = new Clock(ac, 200f);
		ac.out.addDependent(clock);
		
		JFrame frame = new JFrame();
		final PatternGrid pg = new PatternGrid(10,10);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.getContentPane().add(pg.getComponent());
		frame.pack();
		
		clock.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				if(clock.isBeat()) {
					ArrayList<Integer> notes = pg.getPattern().getEventAtStep(clock.getBeatCount());
					//play note
					if(notes == null) return;
					for(int i : notes) {
						WavePlayer wp = new WavePlayer(ac, Pitch.mtof(i + 60), Buffer.SINE);
						Envelope e = new Envelope(ac, 0.2f);
						Gain g = new Gain(ac, 1, e);
						g.addInput(wp);
						e.addSegment(0f, 200f, new KillTrigger(g));
						ac.out.addInput(g);
					}
				}
			}
		});
		
		frame.setVisible(true);
		ac.start();
		
	}
}
