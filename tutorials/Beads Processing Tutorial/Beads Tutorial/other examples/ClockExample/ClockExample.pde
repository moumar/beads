import net.beadsproject.beads.events.*;
import net.beadsproject.beads.data.*;
import net.beadsproject.beads.ugens.*;
import net.beadsproject.beads.analysis.segmenters.*;
import net.beadsproject.beads.analysis.featureextractors.*;
import net.beadsproject.beads.analysis.*;
import net.beadsproject.beads.data.buffers.*;
import net.beadsproject.beads.core.*;



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

