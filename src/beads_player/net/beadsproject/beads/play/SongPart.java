package net.beadsproject.beads.play;

import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.beadsproject.beads.events.KeyInput;
import net.beadsproject.beads.gui.BeadsPanel;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;



// TODO: Auto-generated Javadoc
/**
 * The Class SongPart. 
 */
public abstract class SongPart extends Gain implements InterfaceElement {

	/** The params. */
	protected ArrayList<InterfaceElement> interfaceElements;
	
	/** The panel. */
	protected JComponent panel;
	
	protected int state;
	
	protected Clock clock;
	
	protected Environment environment;
	
	public SongPart(String name, Environment environment) {
		this(name, environment, 2);
		setGainEnvelope(new Envelope(getContext(), 2));
	}
	
	/**
	 * Instantiates a new song part.
	 * 
	 * @param environment
	 *            the environment
	 * @param inouts
	 *            the inouts
	 */
	protected SongPart(String name, Environment environment, int inouts) {
		super(environment.ac, inouts);
		this.environment = environment;
		setName(name);
		interfaceElements = new ArrayList<InterfaceElement>();
		pause(true);
		state = 0;
//		setGainEnvelope(new Envelope(context, 0f));
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	/**
	 * Setup panel.
	 */
	private void setupPanel() {
		panel = new BeadsPanel();
		((BeadsPanel)panel).horizontalBox();
		for(InterfaceElement p : interfaceElements) {
			panel.add(p.getComponent());
		}
		if(this instanceof KeyListener) {
			System.out.println("KEY LISTENER");
			panel.addKeyListener((KeyListener)this);
		}
	}
	
	/**
	 * Gets the panel.
	 * 
	 * @return the panel
	 */
	public JComponent getComponent() {
		if(panel == null) setupPanel();
		return panel;
	}
	
	public abstract void enter();
	
	public abstract void exit();
	
	public final String toString() {
		return getName() + " (" + getClass().getSimpleName() + ")";
	}

	
	public Clock getClock() {
		return clock;
	}

	
	public void setClock(Clock clock) {
		this.clock = clock;
	}
	
	
}
