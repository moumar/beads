package net.beadsproject.beads.data.buffers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;

/**
 * A buffer of random floats.
 * 
 * @author ben
 *
 */
class NoiseBuffer extends BufferFactory
{
  public Buffer generateBuffer(int bufferSize) {
    Buffer b = new Buffer(bufferSize);
    for(int i = 0; i < bufferSize; i++) {
      b.buf[i] = (float)(1.-2.*Math.random());
    }
    return b;
  }

  public String getName() {
    return "Noise";
  }

};