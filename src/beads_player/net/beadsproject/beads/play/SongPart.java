package net.beadsproject.beads.play;

import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import net.beadsproject.beads.events.PauseTrigger;
import net.beadsproject.beads.gui.BeadsPanel;
import net.beadsproject.beads.gui.LevelMeter;
import net.beadsproject.beads.gui.Slider;
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
	
	protected Gain controllableGain;
	
	protected Environment environment;
	
	public SongPart(String name, Environment environment) {
		this(name, environment, 2);
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
		interfaceElements.add(new LevelMeter(this));
		controllableGain = new Gain(context, inouts);
		Slider s = new Slider(context, "gain", 0f, 1f, 0f);
		controllableGain.setGainEnvelope(s);
		addInput(controllableGain);
		interfaceElements.add(s);
		setGainEnvelope(new Envelope(context, 0f));
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	/**
	 * Setup panel.
	 */
	private void setupPanel() {
		panel = new BeadsPanel();
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
	
	public void enter() {
		((Envelope)getGainEnvelope()).lock(false);
		getGainEnvelope().setValue(1f);
	}

	public void exit() {
		((Envelope)getGainEnvelope()).clear();
		((Envelope)getGainEnvelope()).addSegment(0f, 500f, new PauseTrigger(this));
		((Envelope)getGainEnvelope()).lock(true);
	}
	
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
