package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

class TapOut extends UGen
{
  private TapIn ti;
  private UGen sampleDelayEnvelope;

  public TapOut(AudioContext ac, TapIn ti, int sampleDelay)
  {
    super(ac,0,1);
    this.ti = ti;
    this.sampleDelayEnvelope = new Static(ac,sampleDelay);    
    this.addDependent(ti);
  }

  public TapOut(AudioContext ac, TapIn ti, UGen sampleDelayEnvelope)
  {
    super(ac,0,1);
    this.ti = ti;
    this.sampleDelayEnvelope = sampleDelayEnvelope;    
    this.addDependent(ti);
  }

  public void calculateBuffer()
  {
    sampleDelayEnvelope.update();
    ti.fillBufferFrom(bufOut[0],sampleDelayEnvelope);
  }  
};