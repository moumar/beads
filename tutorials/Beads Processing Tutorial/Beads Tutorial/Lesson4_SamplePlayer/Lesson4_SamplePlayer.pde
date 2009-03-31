import net.beadsproject.beads.events.*;
import net.beadsproject.beads.data.*;
import net.beadsproject.beads.ugens.*;
import net.beadsproject.beads.analysis.segmenters.*;
import net.beadsproject.beads.analysis.featureextractors.*;
import net.beadsproject.beads.analysis.*;
import net.beadsproject.beads.data.buffers.*;
import net.beadsproject.beads.core.*;

AudioContext ac;

void setup() {
  size(300,300);
  ac = new AudioContext();
  /*
   * Here's how to play back a sample.
   * 
   * The first line gives you a way to choose the audio file.
   * The second line creates a sample player and loads in the Sample.
   * SampleManager is a utility which keeps track of loaded audio
   * files according to their file names, so you don't have to load them again.
   */
  String audioFile = selectInput();
  SamplePlayer player = new SamplePlayer(ac, SampleManager.sample(audioFile));
  /*
   * And as before...
   */
  Gain g = new Gain(ac, 2, 0.2);
  g.addInput(player);
  ac.out.addInput(g);
  ac.start();
}


/*
 * Here's the code to draw a scatterplot waveform.
 * The code draws the current buffer of audio across the
 * width of the window. To find out what a buffer of audio
 * is, read on.
 * 
 * Start with some spunky colors.
 */
color fore = color(255, 102, 204);
color back = color(0,0,0);

/*
 * Just do the work straight into Processing's draw() method.
 */
void draw() {
  loadPixels();
  //set the background
  Arrays.fill(pixels, back);
  //scan across the pixels
  for(int i = 0; i < width; i++) {
    //for each pixel work out where in the current audio buffer we are
    int buffIndex = i * ac.getBufferSize() / width;
    //then work out the pixel height of the audio data at that point
    int vOffset = (int)((1 + ac.out.getValue(0, buffIndex)) * height / 2);
    //draw into Processing's convenient 1-D array of pixels
    pixels[vOffset * height + i] = fore;
  }
  updatePixels();
}
