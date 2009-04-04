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
  System.out.println("HEY!!!!!");
  System.out.println("Before you run this example, point the SampleManager code to an audio file (and then delete the System.exit(1) command!!!).");
  System.exit(1);
  AudioContext ac = new AudioContext(512);
  Sample s1 = SampleManager.sample("/Users/ollie/Programming/Eclipse/Beads/audio/1234.aif");	
  System.out.println(s1.length);
  SamplePlayer sp = new SamplePlayer(ac, s1); 	
  Envelope rateEnv = new Envelope(ac, 1f);
  rateEnv.addSegment(4f, 5000f);
  sp.setRateEnvelope(rateEnv);
  sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING); 
  sp.getLoopEndEnvelope().setValue(1000f);
  sp.getLoopStartEnvelope().setValue(500f);  	
  ac.out.addInput(sp);
  ac.start();
}





