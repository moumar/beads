package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Converts a signal to have n-bits, useful for 8-bit synthesis.
 * PRE: Signal must be in (-1,1) range
 * POST: Signal is in (-1,1) range 
 * 
 * @author ben
 *
 */
public class NBitsConverter extends UGen
{
	  private int toRange;
	  private float invToRange;

	  public NBitsConverter(AudioContext ac, int n)
	  {
	    super(ac,1,1);
	    toRange = 1<<(n-1);
	    invToRange = (float) (1. / toRange);

	  }

	  public void calculateBuffer()
	  {
	    // for each float value (-1,1)
	    // map it to -toRange,toRange
	    // and truncate 
	    // then map back    

	    for(int i=0;i<bufferSize;i++)
	    {
	      bufOut[0][i] = invToRange * (int)(bufIn[0][i] * toRange);
	    }
	  }
};
	
