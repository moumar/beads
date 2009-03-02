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

/*
 * 
 */

void setup() {
  AudioContext ac = new AudioContext(512);
  Clock clock = new Clock(ac, new Static(ac, 500f));
  ac.out.addDependent(clock);
  clock.setClick(true);
  clock.addMessageListener(new ClockListener());
  ac.start();
}

private static class ClockListener extends Bead {
  public void messageReceived(Bead message) {
    Clock clock = (Clock)message;
    System.out.println(clock.getCount());
  }
}

