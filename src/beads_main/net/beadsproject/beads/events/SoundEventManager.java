package net.beadsproject.beads.events;

import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import net.beadsproject.beads.core.UGen;


public class SoundEventManager {

	@SuppressWarnings("unchecked")
	public static Map<String, Object> paramsFromXML(InputStream is) {
		Map<String, Object> parameters = null;
		try {
			XMLDecoder xml = new XMLDecoder(is, null, null);
			parameters = (Map<String, Object>)xml.readObject();
			xml.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parameters;
	}
	
	public static Map<String, Object> paramsFromXML(String filename) {
		try {
			File file = new File(filename);
			FileInputStream fis;
			fis = new FileInputStream(file);
			Map<String, Object> parameters = paramsFromXML(fis);
			fis.close();
			return parameters;	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void play(UGen output, Map<String, Object> parameters) {
		try {
			Class<? extends SoundEvent> soundEventClass = (Class<? extends SoundEvent>)parameters.get("class");
			if(soundEventClass == null) System.out.println("could not find class for SoundEvent");
			Method playMethod = soundEventClass.getMethod("play", UGen.class, Map.class);
			SoundEvent event = soundEventClass.newInstance();
			playMethod.invoke(event, output, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void play(UGen output, InputStream is, Map<String, Object> parameters) {
		Map<String, Object> parametersFromFile = paramsFromXML(is);
		parametersFromFile.putAll(parameters);
		play(output, parametersFromFile);
	}
	
	public static void play(UGen output, String s, Map<String, Object> parameters) {
		Map<String, Object> parametersFromFile = paramsFromXML(s);
		parametersFromFile.putAll(parameters);
		play(output, parametersFromFile);
	}
	
}
