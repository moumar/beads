/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.UGen;


// TODO: Auto-generated Javadoc
/**
 * The Class MouseResponder.
 */
public class MouseResponder extends UGen {

	/** The point. */
	private Point point;
	
	/** The x. */
	public float x;
	
	/** The y. */
	public float y;
	
	private float currentX;
	private float currentY;
	
	/** The width. */
	private int width;
	
	/** The height. */
	private int height;
	
	/**
	 * Instantiates a new mouse responder.
	 * 
	 * @param context
	 *            the context
	 */
	public MouseResponder(AudioContext context) {
		super(context);
		width = Toolkit.getDefaultToolkit().getScreenSize().width;
		height = Toolkit.getDefaultToolkit().getScreenSize().height;
	}

	/**
	 * Gets the point.
	 * 
	 * @return the point
	 */
	public Point getPoint() {
		return point;
	}

	/**
	 * Sets the point.
	 * 
	 * @param point
	 *            the new point
	 */
	public void setPoint(Point point) {
		this.point = point;
	}


	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		point = MouseInfo.getPointerInfo().getLocation();
		x = (float)point.x / (float)width;
		y = (float)point.y / (float)height;
	}

}
