import net.beadsproject.beads.events.*;
import net.beadsproject.beads.data.*;
import net.beadsproject.beads.ugens.*;
import net.beadsproject.beads.analysis.segmenters.*;
import net.beadsproject.beads.analysis.featureextractors.*;
import net.beadsproject.beads.analysis.*;
import net.beadsproject.beads.data.buffers.*;
import net.beadsproject.beads.core.*;

AudioContext ac;
PowerSpectrum ps;

void setup() {
  size(300,300);
  ac = new AudioContext();
  /*
   * Here's something we've seen before...
   */
  String audioFile = selectInput();
  SamplePlayer player = new SamplePlayer(ac, SampleManager.sample(audioFile));
  Gain g = new Gain(ac, 2, 0.2);
  g.addInput(player);
  ac.out.addInput(g);
  /*
   * To analyse a signal, build an analysis chain.
   */
  ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
  sfs.addInput(ac.out);
  FFT fft = new FFT();
  ps = new PowerSpectrum();
  sfs.addListener(fft);
  fft.addListener(ps);
  ac.out.addDependent(sfs);
  //and begin
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
  //get the features
  float[] features = ps.getFeatures();
  if(features != null) {
    //scan across the pixels
    for(int i = 0; i < width; i++) {
      //for each pixel work out where in the current audio buffer we are
      int featureIndex = i * features.length / width;
      //then work out the pixel height of the feature data at that point
      int vOffset = height - 1 - Math.min((int)(features[featureIndex] * height), height - 1);
      //draw into Processing's convenient 1-D array of pixels
      pixels[vOffset * height + i] = fore;
    }
  }
  updatePixels();
}
