package net.beadsproject.beads.ugens;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sound.sampled.UnsupportedAudioFileException;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.core.Clock;
import net.beadsproject.beads.ugens.core.Gain;
import net.beadsproject.beads.ugens.sample.SamplePlayer;

public class ManySamplesLoaded {

	/**
	 * Test loading many samples and playing with Sample.regionMaster load.
	 */
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		AudioContext ac = new AudioContext();
		// ac.checkForDroppedFrames(false);
		
		Sample.Regime tr = new Sample.TimedRegime(100,100,0,-1,Sample.TimedRegime.Order.ORDERED);
		// tr = Sample.Regime.newTotalRegime();
			
		SampleManager.group("piano", "D:/audio/crash test audio/chopped live sounds/piano board");
		SampleManager.setBufferingRegime(tr);
		int NUM_SAMPLES = SampleManager.getGroup("piano").size();
		NUM_SAMPLES = Math.min(40,NUM_SAMPLES);
		
		// 	test different schedulers
		// Sample.regionMaster = Executors.newSingleThreadExecutor();
		// Sample.regionMaster = Executors.newCachedThreadPool();
		Sample.regionMaster = Executors.newFixedThreadPool(NUM_SAMPLES/2);
		
		final Gain g = new Gain(ac, 2, 10f / NUM_SAMPLES);
		for(int i = 0; i < NUM_SAMPLES; i++) {
			SamplePlayer wp = new SamplePlayer(ac, SampleManager.fromGroup("piano", i));
			wp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
			wp.setKillOnEnd(false);
			wp.getRateEnvelope().setValue((float)Math.random()*0.01f + 1.f);
			g.addInput(wp);
		}
		ac.out.addInput(g);		
		
		try
		{
			final ThreadPoolExecutor tpe = ((ThreadPoolExecutor)Sample.regionMaster);
			System.out.printf("thread pool size = %d\n", tpe.getMaximumPoolSize());
			
			// print out some regionMaster info...
			Clock cl = new Clock(ac,100);
			cl.addMessageListener(new Bead(){
				public void messageReceived(Bead msg)
				{
					if (((Clock)(msg)).isBeat())
					{
						System.out.printf("%d %d %d %d\n",
								tpe.getPoolSize(),
								tpe.getActiveCount(),
								tpe.getCompletedTaskCount(),
								tpe.getTaskCount());					
					}
				}
			});		
			ac.out.addDependent(cl);
		}
		catch(Exception e)
		{
			// pass
		}
		
		ac.start();
	}

}
