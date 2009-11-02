package net.beadsproject.beads.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
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
	Gain sm;
	Sample s;
	List<SamplePlayer> sps;
	
	
	public TestUGenInputsAtChannel()
	{
		ac = new AudioContext();
		//ac.setThreadPriority(Thread.NORM_PRIORITY);
		
		System.out.println("Testing concurrent modification of UGen.inputsAtChannel. \n" +
			"Test runs indefinitely until you quit or an error is detected.");
		sm = new Gain(ac,2);
		s = SampleManager.sample("audio/1234.aif");
		sps = new ArrayList<SamplePlayer>();
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
						Thread.sleep(50);
							
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
							
						}
						verifyInputs();
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
		sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING); //<--------- hey Ben, without this sample players die automatically, triggering the error
		sm.addInput(sp);
		sps.add(sp);
		return sp;		
	}
	
	public void removeRandomPlayer()
	{
		System.out.println("Removing sp");
		SamplePlayer sp = sps.get((int)(Math.random()*sps.size()));
		if(sp == null) System.out.println("NULL");
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
		System.out.println("numsps = " + numsps);
		boolean failed = false;
		for(int i=0;i<sm.getIns();i++)
		{
			int n = sm.getNumberOfConnectedUGens(i);	
			System.out.println(n);
			
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
