package net.beadsproject.beads.ugens;

import java.util.Arrays;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

public class TapIn extends UGen
{
  private float buffer[];
  private int counter;

  /**
   * 
   * @param ac AudioContext
   * @param bufferSizeInSamples The size of the tapin buffer 
   */
  public TapIn(AudioContext ac, int bufferSizeInSamples)
  {
    super(ac,1,0);
    buffer = new float[bufferSizeInSamples];     
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
      float numSamplesBack = env.getValue(0,i);      
      numSamplesBack = numSamplesBack%buffer.length;
      int index = (int)((counter - numSamplesBack - bufferSize)%buffer.length + buffer.length + i)%buffer.length;
      buf[i] = buffer[index];
    } 
  }
};