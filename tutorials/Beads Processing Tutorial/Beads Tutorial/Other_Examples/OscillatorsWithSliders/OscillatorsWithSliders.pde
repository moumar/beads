import net.beadsproject.beads.core.*;
import net.beadsproject.beads.data.buffers.*;
import net.beadsproject.beads.ugens.*;
import controlP5.*;

ControlP5 controlP5;
AudioContext ac;

final int NUMOSC = 10;
WavePlayer oscillators[];

void setup() {
  size(400,100);
  background(0,0,0);
  
  controlP5 = new ControlP5(this);

  controlP5.setColorValue(0);
  controlP5.setColorBackground(0);
  controlP5.setColorLabel(0);
    
  ac = new AudioContext();	
  ScalingMixer sm = new ScalingMixer(ac);
  oscillators = new WavePlayer[NUMOSC];
  int border = 10;
  int gap = 2;
  float di = (width-2.*border-gap*(NUMOSC-1))/NUMOSC;
  for(int i=0;i<NUMOSC;i++)
  {
    float val = random(10,1000);
    controlP5.addSlider(Integer.toString(i),10,1000,val,(int)(i*(di+gap))+border,border,(int)di,height-2*border).setId(i);
    WavePlayer wp = new WavePlayer(ac, val, new SquareBuffer().getDefault());
    oscillators[i] = wp;
    sm.addInput(wp);
  }    
  
  ac.out.addInput(sm);
  ac.start();
}

void draw()
{
  background(0);  
}

void controlEvent(ControlEvent theEvent) {
  int i = theEvent.controller().id();
  float v = theEvent.controller().value();
  ((Static)oscillators[i].getFrequencyEnvelope()).setValue(v);
}
