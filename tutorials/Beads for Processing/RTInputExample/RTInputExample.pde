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
  AudioContext ac = new AudioContext();
  RTInput input = new RTInput(ac, new AudioFormat(44100, 16, 2, true, true));
  ac.out.addInput(input);
  ac.start();
}




