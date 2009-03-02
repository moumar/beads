import net.beadsproject.beads.events.*;
import net.beadsproject.beads.data.*;
import net.beadsproject.beads.ugens.*;
import net.beadsproject.beads.analysis.segmenters.*;
import net.beadsproject.beads.analysis.featureextractors.*;
import net.beadsproject.beads.analysis.*;
import net.beadsproject.beads.data.buffers.*;
import net.beadsproject.beads.core.*;

void setup() {
  AudioContext ac = new AudioContext();
  /*
   * This is an Envelope. It can be used to modify
   * the behaviour of other UGen object. We need to
   * do this to get precise control of certain parameters
   * at an audio rate.
   */
  Envelope freqEnv = new Envelope(ac, 500f);
  /*
   * This is a WavePlayer. Here we've set it up using 
   * the above Envelope, and a SineBuffer. We'll use
   * the Envelope to modify the freqency below.
   */
  WavePlayer wp = new WavePlayer(ac, freqEnv, new SineBuffer().getDefault());
  /*
   * So now that the WavePlayer is set up with the 
   * frequency Envelope, do stuff with the frequency
   * envelope. This command tells the Envelope to change
   * to 1000 in 1 second. Note that when we made the Envelope
   * it was set to 500, so the transition goes from 500 to
   * 1000. These control the frequency of the WavePlayer
   * in Hz.
   */
  freqEnv.addSegment(1000f, 1000f);
  /*
   * Connect it all together as before.
   */
  Gain g = new Gain(ac, 1, 0.1f);
  g.addInput(wp);
  ac.out.addInput(g);
  ac.start();
}

void draw() {
}
