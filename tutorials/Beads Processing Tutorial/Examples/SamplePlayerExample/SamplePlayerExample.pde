import com.olliebown.beads.analysis.featureextractors.*;
import com.olliebown.beads.data.buffers.*;
import com.olliebown.beads.events.*;
import com.olliebown.beads.analysis.*;
import com.olliebown.beads.play.*;
import com.olliebown.beads.core.*;
import com.olliebown.beads.analysis.segmenters.*;
import com.olliebown.beads.ugens.*;
import com.olliebown.beads.data.*;
import com.olliebown.beads.gui.*;
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





