package net.beadsproject.beads.data;

import net.beadsproject.beads.core.AudioContext;

public class UsingSample {

	public static class Basic {
		public static void main(String[] args) throws Exception {
			/*
			 * Load a sample from an audio file.
			 */
			Sample s = new Sample("audio/1234.aif");
			System.out.println("Loaded sample from file");
			System.out.println("Name " + s.getFileName());
			System.out.println("Known to friends as " + s.getSimpleFileName());
			System.out.println("Length " + s.getLength() + "ms");
			System.out.println("Channels " + s.getNumChannels());
			System.out.println("----------------");
			/*
			 * Or create a new empty sample.
			 */
			AudioContext ac = new AudioContext();
			s = new Sample(ac.getAudioFormat(), 1000);
			System.out.println("Created new Sample");
			System.out.println("Name " + s.getFileName());
			System.out.println("Known to friends as " + s.getSimpleFileName());
			System.out.println("Length " + s.getLength() + "ms");
			System.out.println("Channels " + s.getNumChannels());
			System.out.println("----------------");
			/*
			 * Resize if you like. Note that the resize command
			 * works in frames, to be more specific.
			 */
			s.resize((long)ac.msToSamples(2000));
			System.out.println("Resized sample");
			System.out.println("Name " + s.getFileName());
			System.out.println("Known to friends as " + s.getSimpleFileName());
			System.out.println("Length " + s.getLength() + "ms");
			System.out.println("Channels " + s.getNumChannels());
			System.out.println("----------------");
		}
	}
	
	public static class UsingSampleManager {
		public static void main(String[] args) {
			/*
			 * SampleManager is a singleton, which means that you
			 * can just access all of its methods statically (no
			 * need to instantiate a SampleManager).
			 * 
			 * When you load a Sample through SampleManager it stores
			 * it in a Hashtable by its filename.
			 * 
			 * SampleManager doesn't throw exceptions, it just prints
			 * the error trace.
			 */
			SampleManager.sample("audio/1234.aif");
			/*
			 * Notice that we're making the same call as before here
			 * but that the sample has already been loaded, so SampleManager
			 * doesn't load it again, but instead just retrieves it
			 * from its Hashtable.
			 * 
			 * The key to the Hashtable will be whatever relative or
			 * absolute path you enter here.
			 */
			Sample s = SampleManager.sample("audio/1234.aif");
			/*
			 * If you want you can specify the name to save the Sample with.
			 * Note in this case the sample data gets reloaded. SampleManager doesn't
			 * track which files have been loaded except by the names they
			 * are stored under.
			 */
			SampleManager.sample("freestyle", "audio/1234.aif");
			/*
			 * You can also load groups of Samples into a named group. Again,
			 * the sample gets loaded this time because while searching
			 * through the folder its name gets renamed as an absolute path.
			 */
			SampleManager.group("mySounds", "audio");
			/*
			 * You can access Samples from groups in various ways.
			 */
			s = SampleManager.randomFromGroup("mySounds");
			s = SampleManager.fromGroup("mySounds", 0); //loops index
			/*
			 * Then there are some deleting methods.
			 */
			SampleManager.removeSample(s);
			SampleManager.removeSample("audio/1234.aif");
			SampleManager.removeGroup("mySounds");
		}
	}
	
}
