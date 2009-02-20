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

void setup() {
  Random rng = new Random();
  AudioContext ac = new AudioContext(512, 5000);
  int numOscs = 80;
  float[] freqs = new float[numOscs];
  for(int i = 0; i < numOscs; i++) {
    freqs[i] = rng.nextFloat() * 5000f + 100f;
  }
  OscillatorBank ob = new OscillatorBank(ac, new SineBuffer().getDefault(), numOscs);
  ob.setFrequencies(freqs);
  ac.out.addInput(ob);
  ac.start();
}



