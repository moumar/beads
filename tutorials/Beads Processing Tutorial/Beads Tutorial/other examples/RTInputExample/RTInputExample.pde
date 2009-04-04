import net.beadsproject.beads.events.*;
import net.beadsproject.beads.data.*;
import net.beadsproject.beads.ugens.*;
import net.beadsproject.beads.analysis.segmenters.*;
import net.beadsproject.beads.analysis.featureextractors.*;
import net.beadsproject.beads.analysis.*;
import net.beadsproject.beads.data.buffers.*;
import net.beadsproject.beads.core.*;


import javax.sound.sampled.AudioFormat;

void setup() {
  AudioContext ac = new AudioContext();
  RTInput input = new RTInput(ac, new AudioFormat(44100, 16, 2, true, true));
  ac.out.addInput(input);
  ac.start();
}




