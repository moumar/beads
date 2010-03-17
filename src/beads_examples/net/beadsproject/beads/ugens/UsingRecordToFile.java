package net.beadsproject.beads.ugens;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.RecordToFile;
import net.beadsproject.beads.ugens.SamplePlayer;

public class UsingRecordToFile {
	
	public static class BasicRecording {
		public static void main(String[] args) throws IOException {
			/*
			 * Record a few seconds of a looped file into "output.wav".
			 */			
			final float THREE_SECONDS = 3000f;
			final String SAMPLE = "audio/1234.aif";
			final String OUTPUT_FILE = "output/output.wav";
			
			final AudioContext ac = new AudioContext();
			SampleManager.setAudioContext(ac);
			Sample loop = SampleManager.sample(SAMPLE);
			SamplePlayer sp = new SamplePlayer(ac, loop);
			sp.setLoopType(SamplePlayer.LoopType.LOOP_BACKWARDS);			
			ac.out.addInput(sp);
			
			final RecordToFile rtf = new RecordToFile(ac, loop.getNumChannels(), DeleteFileAndGetNewOne(OUTPUT_FILE));
			rtf.addInput(sp);
			ac.out.addDependent(rtf);						
			
			/*
			 * We have to explicitly "kill" the file recorder to finalise writing out the file header.
			 * In this example, we do this after a specified delay. 
			 */
			DelayTrigger dt = new DelayTrigger(ac, THREE_SECONDS, new Bead()
			{
				protected void messageReceived(Bead b)
				{
					
					rtf.kill();
					ac.stop();
				}				
			});
			ac.out.addDependent(dt);
			ac.start();			
		}
	}
	
	public static class AllTypesRecording {
		public static void main(String[] args) throws IOException {
			/*
			 * Try to record simultaneously into all the supported audio formats on this machine.
			 * Currently, any unsupported formats will generate garbage files.
			 */
			System.out.println("This example demonstrates how RecordToFile will support different file formats in the future.\n" +
					"Currently, however, it only supports .wav files, and so the other outputted files may contain junk.\n");
			
			final float THREE_SECONDS = 3000f;
			final String SAMPLE = "audio/1234.aif";
			final String OUTPUT_PREFIX = "output/output_all_types";
			
			final AudioContext ac = new AudioContext();
			SampleManager.setAudioContext(ac);
			Sample loop = SampleManager.sample(SAMPLE);
			SamplePlayer sp = new SamplePlayer(ac, loop);
			sp.setLoopType(SamplePlayer.LoopType.LOOP_BACKWARDS);			
			ac.out.addInput(sp);
			
			final List<RecordToFile> rtfs = new LinkedList<RecordToFile>();
			
			for(AudioFileFormat.Type afft: AudioSystem.getAudioFileTypes())
			{
				try
				{
					RecordToFile rtf = new RecordToFile(ac, loop.getNumChannels(), DeleteFileAndGetNewOne(OUTPUT_PREFIX+"."+afft.getExtension()),afft);
					rtf.addInput(sp);
					ac.out.addDependent(rtf);
					rtfs.add(rtf);				
				}
				catch(IOException e)
				{
					e.printStackTrace();					
				}
			}									
			
			DelayTrigger dt = new DelayTrigger(ac, THREE_SECONDS, new Bead()
			{
				protected void messageReceived(Bead b)
				{
					/*
					 * Note that we have to kill ALL the recorders.
					 */
					for(RecordToFile rtf: rtfs)
						rtf.kill();
					
					ac.stop();
				}				
			});
			ac.out.addDependent(dt);
			ac.start();			
		}
	}
	
	/**
	 * Clear the contents of fileName and return it as a brand new empty file.
	 * 
	 * @param fileName 
	 * @return The file object.
	 */
	public static File DeleteFileAndGetNewOne(String fileName)
	{
		File f = new File(fileName);
		if (!f.exists()) return f;
		else
		{
			if (!f.canWrite())
			{
				throw new IllegalArgumentException("Delete: write protected: " + fileName);
			}	
			else
			{
				 boolean success = f.delete();
				 if (!success)
				 {
					 throw new IllegalArgumentException("Delete: deletion failed. " + fileName);
				 }				 
			}
		}
		
		return f;
	}
}
