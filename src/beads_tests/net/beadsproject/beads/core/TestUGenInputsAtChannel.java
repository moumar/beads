package net.beadsproject.beads.core;

import java.util.HashSet;
import java.util.Set;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.ScalingMixer;

/**
 * Tests the concurrent modification of a ugen's inputs.
 * Coded by ben because he was getting a weird bug.
 * 
 * @author ben
 *
 */
public class TestUGenInputsAtChannel {

	AudioContext ac;
	ScalingMixer sm;
	Sample s;
	Set<SamplePlayer> sps;
	
	
	public TestUGenInputsAtChannel()
	{
		ac = new AudioContext();
		//ac.setThreadPriority(Thread.NORM_PRIORITY);
		
		System.out.println("Testing concurrent modification of UGen.inputsAtChannel. \n" +
			"Test runs indefinitely until you quit or an error is detected.");
		sm = new ScalingMixer(ac,2);
		s = SampleManager.sample("audio/1234.aif");
		sps = new HashSet<SamplePlayer>();
		ac.out.addInput(sm);
	}
	
	public void run()
	{
		// start a thread that loops, either adding a new sample player or removing one
		// after removal we check the number of inputs at each channel
		Thread t = new Thread()
		{
			public void run() {
				try {					
					while (ac.isRunning())
					{
						Thread.sleep(100);
							
						if (sps.isEmpty())
						{
							addSamplePlayer();
						}
						else 
						{
							if (Math.random()>.5)
							{
								addSamplePlayer();
							}
							else
							{
								removeRandomPlayer();
							}
							
							verifyInputs();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}		
		};
		
		ac.start();
		t.start();
	}
	
	public SamplePlayer addSamplePlayer()
	{
		System.out.println("Adding sp");
		SamplePlayer sp = new GranularSamplePlayer(ac, SampleManager.sample("audio/1234.aif"));
		sm.addInput(sp);
		sps.add(sp);
		return sp;		
	}
	
	public void removeRandomPlayer()
	{
		System.out.println("Removing sp");
		
		SamplePlayer[] spa = sps.toArray(new SamplePlayer[]{});
		SamplePlayer sp = spa[(int)(Math.random()*spa.length)];
		sps.remove(sp);
		removeSamplePlayer(sp);
	}
	
	public void removeSamplePlayer(SamplePlayer sp)
	{
		sm.removeAllConnections(sp);
	}
	
	public void verifyInputs()
	{
		int numsps = sps.size();
		System.out.printf("numsps = %d, numinputs = ", numsps);
		boolean failed = false;
		for(int i=0;i<sm.getIns();i++)
		{
			int n = sm.getNumberOfConnectedUGens(i);	
			System.out.printf("%d, ", n);
			
			if (n!=numsps)
			{
				failed = true;
			}
		}
		System.out.println();
		
		if (failed)
		{
			System.out.println("Test failed!");
			System.exit(1);
		}
	}
	
	static public void main(String[] args)
	{
		TestUGenInputsAtChannel test = new TestUGenInputsAtChannel();
		test.run();
	}
	
	
}
