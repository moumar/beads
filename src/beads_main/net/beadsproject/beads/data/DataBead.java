package net.beadsproject.beads.data;

import net.beadsproject.beads.core.*;
import java.util.*;

/**
 * A bead that stores properties as key/value pairs. Keys must be Strings, and
 * values may be any Object. Implements the Map interface.
 * 
 * @author Benito Crawford
 * @version 0.9
 */
public class DataBead extends Bead implements Map<String, Object> {
	public Hashtable<String, Object> properties;

	/**
	 * Creates a DataBead instance with no defined properties.
	 */
	public DataBead() {
		this(null);
	}

	/**
	 * Creates a DataBead instance with properties specified by a String array
	 * that are set to corresponding values specified by an Object array.
	 * 
	 * @param proparr
	 *            The array of property names.
	 * @param valarr
	 *            The array of Object values.
	 * 
	 */
	public DataBead(String[] proparr, Object[] valarr) {
		properties = new Hashtable<String, Object>();

		if (proparr != null && valarr != null) {
			int s = Math.min(proparr.length, valarr.length);
			for (int i = 0; i < s; i++) {
				if (proparr[i] != null)
					properties.put(proparr[i], valarr[i]);
			}
		}
	}

	/**
	 * Creates a DataBead instance that uses a Hashtable for its properties.
	 * (This does not copy the input Hashtable, so any changes to it will change
	 * the properties of the DataBead!)
	 * 
	 * @param ht
	 *            The input Hashtable.
	 */
	public DataBead(Hashtable<String, Object> ht) {
		if (ht == null) {
			properties = new Hashtable<String, Object>();
		} else {
			properties = ht;
		}
	}

	/**
	 * If the input message is a DataBead, this adds the properties from the
	 * message Bead to this one. (Equivalent to {@link #putAll(DataBead)} .)
	 */
	public void messageReceived(Bead message) {
		if (message instanceof DataBead) {
			putAll(((DataBead) message).properties);
		}
	}

	/**
	 * Adds the properties from the input DataBead to this one.
	 * 
	 * @param db
	 *            The input DataBead.
	 */
	public void putAll(DataBead db) {
		putAll(db.properties);
	}

	/**
	 * Gets a float representation of the specified property; returns the
	 * specified default value if that property doesn't exist or cannot be cast
	 * as a float.
	 * <p>
	 * This method is a useful way to update <code>float</code> parameters in a
	 * class:
	 * <p>
	 * <code>float param = startval;<br>
	 * ...<br>
	 * <code>param = databead.getFloat("paramKey", param);</code>
	 * 
	 * @param key
	 * @param defaultVal
	 * @return The property value, or the default value if there is no float
	 *         representation of the property.
	 */
	public float getFloat(String key, float defaultVal) {

		Object o = get(key);
		if (o instanceof Number) {
			return ((Number) o).floatValue();
		} else if (o instanceof String) {
			try {
				float r = Float.parseFloat((String) o);
				return r;
			} catch (Exception e) {
			}
		} else if (o instanceof Boolean) {
			if ((Boolean) o == true) {
				return 1;
			} else {
				return 0;
			}
		}

		return defaultVal;
	}

	/**
	 * Returns the UGen value for the specified key. If the value stored at the
	 * key is not a UGen, it returns <code>null</code>.
	 * 
	 * @param key
	 *            The key.
	 * @return The UGen if it exists.
	 */
	public UGen getUGen(String key) {
		Object o = get(key);
		if (o instanceof UGen) {
			return (UGen) o;
		} else {
			return null;
		}
	}

	/**
	 * Returns a new DataBead with a shallow copy of the the original DataBead's
	 * properties.
	 */
	@Override
	public DataBead clone() {
		DataBead ret = new DataBead();
		ret.setName(getName());
		ret.putAll(properties);
		return ret;
	}

	/**
	 * Creates a new DataBead that combines properties from both input
	 * DataBeads. If the same key exists in both, the value from the first one
	 * is used.
	 * 
	 * @param a
	 *            The first input DataBead.
	 * @param b
	 *            The second input DataBead.
	 * @return The new DataBead.
	 */
	public static DataBead combine(DataBead a, DataBead b) {
		DataBead c = new DataBead();
		c.putAll(b);
		c.putAll(a);
		return c;
	}

	@Override
	public String toString() {
		return super.toString() + ":\n" + properties.toString();
	}

	/*
	 * These implement the Map interface methods.
	 */

	public boolean containsKey(Object key) {
		return properties.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return properties.containsValue(value);
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return properties.entrySet();
	}

	public Object get(Object key) {
		return properties.get(key);
	}

	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public Set<String> keySet() {
		return properties.keySet();
	}

	public Object put(String key, Object value) {
		return properties.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		properties.putAll(m);
	}

	public Object remove(Object key) {
		return properties.remove(key);
	}

	public int size() {
		return properties.size();
	}

	public Collection<Object> values() {
		return properties.values();
	}

	public void clear() {
		properties.clear();
	}

}
