/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * SampleManager provides a static repository for {@link Sample} data and provides methods to organise samples into groups.
 */
public class SampleManager {

	/** List of all Samples, indexed by name. */
	private final static Hashtable<String, Sample> samples = new Hashtable<String, Sample>();
	
	/** List of Sample groups, indexed by group name. */
	private final static Hashtable<String, ArrayList<Sample>> groups = new Hashtable<String, ArrayList<Sample>>();

	/**
	 * Returns a new Sample from the given filename. If the Sample has already
	 * been loaded, it will not be loaded again, but will simply be retrieved
	 * from the static repository.
	 * 
	 * @param fn the file path.
	 * 
	 * @return the sample.
	 */
	public static Sample sample(String fn) {
		Sample sample = samples.get(fn);
		if (sample == null) {
			try {
				sample = new Sample(fn);
				samples.put(fn, sample);
			} catch (UnsupportedAudioFileException e) {
				 e.printStackTrace();
			} catch (IOException e) {
				 e.printStackTrace();
			}
		}
		return sample;
	}
	
	/**
	 * Like {@link SampleManager#sample(String)} but with the option to specify the name with which this {@link Sample} is indexed.
	 * 
	 * @param ref the name with which to index this Sample.
	 * @param fn the file path.
	 * 
	 * @return the sample.
	 */
	public static Sample sample(String ref, String fn) {
		Sample sample = samples.get(ref);
		if (sample == null) {
			try {
				sample = new Sample(fn);
				samples.put(ref, sample);
			} catch (UnsupportedAudioFileException e) {
				 e.printStackTrace();
			} catch (IOException e) {
				 e.printStackTrace();
			}
		}
		return sample;
	}

	/**
	 * Generates a new group with the given group name and list of Samples to be
	 * added to the group.
	 * 
	 * @param groupName the group name.
	 * @param sampleList the sample list.
	 */
	public static void group(String groupName, Sample[] sampleList) {
		ArrayList<Sample> group;
		if (!groups.contains(groupName))
			group = new ArrayList<Sample>();
		else
			group = groups.get(groupName);
		for (int i = 0; i < sampleList.length; i++) {
			if (!group.contains(sampleList[i]))
				group.add(sampleList[i]);
		}
	}

	/**
	 * Generates a new group with the given group name and a string that
	 * specifies where to load samples to be added to the group. The string is interpreted firstly as a URL, and if that fails, as a folder path.
	 * 
	 * @param groupName the group name.
	 * @param folderName the folder address (URL or file path).
	 */
	public static void group(String groupName, String folderName) {
		//first try interpreting the folderName as a system resource
		File theDirectory = null;
		try {
			URL url = ClassLoader.getSystemResource(folderName);
			if(url != null) {
				theDirectory = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//failing that, try it as a plain file path
		if(theDirectory == null || !theDirectory.exists()) {
			theDirectory = new File(folderName);
		}
		String[] fileNameList = theDirectory.list();
		for (int i = 0; i < fileNameList.length; i++) {
			fileNameList[i] = theDirectory.getAbsolutePath() + "/" + fileNameList[i];
		}
		group(groupName, fileNameList);
	}

	/**
	 * Generates a new group with the given group name and a list of file names
	 * to be added to the group.
	 * 
	 * @param groupName the group name.
	 * @param fileNameList the file name list.
	 */
	public static void group(String groupName, String[] fileNameList) {
		ArrayList<Sample> group;
		if (!groups.contains(groupName)) {
			group = new ArrayList<Sample>();
			groups.put(groupName, group);
		} else
			group = groups.get(groupName);
		for (int i = 0; i < fileNameList.length; i++) {
			Sample sample = sample(fileNameList[i]);
			if (!group.contains(fileNameList[i]) && sample != null)
				group.add(sample);
		}
	}

	/**
	 * Gets the specified group in the form ArrayList&lt;Sample&gt;.
	 * 
	 * @param groupName the group name.
	 * 
	 * @return the group.
	 */
	public static ArrayList<Sample> getGroup(String groupName) {
		return groups.get(groupName);
	}
	
	/**
	 * Gets a random sample from the specified group.
	 * 
	 * @param groupName the group.
	 * 
	 * @return a random Sample.
	 */
	public static Sample randomFromGroup(String groupName) {
		ArrayList<Sample> group = groups.get(groupName);
		return group.get((int)(Math.random() * group.size()));
	}

	/**
	 * Gets the Sample at the specified index from the specified group.
	 * 
	 * @param groupName the group name.
	 * @param index the index.
	 * 
	 * @return the Sample.
	 */
	public static Sample fromGroup(String groupName, int index) {
		ArrayList<Sample> group = groups.get(groupName);
		return group.get(index % group.size());
	}
	
	/**
	 * Removes the named {@link Sample}.
	 * 
	 * @param sampleName the sample name.
	 */
	public static void removeSample(String sampleName) {
		samples.remove(sampleName);
	}

	/**
	 * Removes the {@link Sample}.
	 * 
	 * @param sample the Sample.
	 */
	public static void removeSample(Sample sample) {
		for (String str : samples.keySet()) {
			if (samples.get(str).equals(sample)) {
				removeSample(str);
				break;
			}
		}
	}

	/**
	 * Removes the specified group, without removing the samples.
	 * 
	 * @param groupName the group name.
	 */
	public static void removeGroup(String groupName) {
		groups.remove(groupName);
	}

	/**
	 * Removes the specified group, and removes all of the samples found in the
	 * group from the sample repository.
	 * 
	 * @param groupName the group name.
	 */
	public static void destroyGroup(String groupName) {
		ArrayList<Sample> group = groups.get(groupName);
		for (int i = 0; i < group.size(); i++) {
			removeSample(group.get(i));
		}
		removeGroup(groupName);
	}

	
	/**
	 * Prints a list of all {@link Sample}s to System.out.
	 */
	public static void printSampleList() {
		for(String s : samples.keySet()) {
			System.out.println(s + " " + samples.get(s));
		}
	}
}
