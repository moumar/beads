package net.beadsproject.beads.ugens;

import java.util.Arrays;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * TapIn stores and serves sound data. Can be used with TapOut to implement delays, etc.
 * 
 * @author ben
 */
public class TapIn extends UGen
{
  private float buffer[];
  private int counter;

  /**
   * @param ac AudioContext
   * @param bufferSizeInMS The size of the tapin buffer in milliseconds. 
   */
  public TapIn(AudioContext ac, float bufferSizeInMS)
  {
    super(ac,1,0);
    buffer = new float[(int)ac.msToSamples(bufferSizeInMS)];     
    Arrays.fill(buffer,0.f);
    counter = 0;
  }

  public void calculateBuffer()
  {
    for(int i=0;i<bufferSize;i++)
    {
      buffer[counter] = bufIn[0][i];
      counter += 1;
      counter %= buffer.length;
    }
  }

  public void fillBufferFrom(float buf[], UGen env)
  {
    for(int i=0;i<buf.length;i++)
    {
      float numSamplesBack = (float)context.msToSamples(env.getValue(0,i));      
      numSamplesBack = numSamplesBack%buffer.length;
    
      float indexf = ((counter - numSamplesBack - bufferSize)%buffer.length + buffer.length + i)%buffer.length;
      int index = (int)indexf;
      float offset = indexf - index;
      //TODO this interpolation doesn't work ... something needs fixing so that when the envelope is in flux
      //the samples sound right
      if(index < buffer.length - 1) {
    	  buf[i] = (1f - offset) * buffer[index] + (offset) * buffer[index + 1]; 
      } else {
    	  buf[i] = buffer[index];
      }
    } 
  }
};