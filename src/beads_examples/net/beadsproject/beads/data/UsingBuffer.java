package net.beadsproject.beads.data;

public class UsingBuffer {
	
	public static class Basics {
		public static void main(String[] args) {
			/*
			 * The main point of Buffer is to provide access
			 * to an array of data. You can make a buffer and
			 * put data into it. It's basically just an array 
			 * of floats.
			 */
			Buffer b = new Buffer(512);
			/*
			 * Put data into the buffer.
			 */
			for(int i = 0; i < b.buf.length; i++) {
				b.buf[i] = (float)i / b.buf.length; //a 0-1 ramp
			}
			/*
			 * You can then access the data by index or
			 * by fraction (thus independent of the size of
			 * the buffer).
			 */
			for(int i = 0; i < b.buf.length; i++) {
				float fraction = (float)i / b.buf.length;
				System.out.println(b.getValueFraction(fraction));
				//or try b.getValueIndex(i);
			}
			/*
			 * The getValue methods return zero for negative indices
			 * and loop their output for positive indices.
			 */
//			for(int i = 0; i < 2 * b.buf.length; i++) {
//				float fraction = (float)i / b.buf.length;
//				System.out.println(b.getValueFraction(fraction));
//			}
			
		}
	}

	public static class DefaultBuffers {
		public static void main(String[] args) {
			/*
			 * These defaults are available.
			 */
			System.out.println(Buffer.SINE);
			System.out.println(Buffer.NOISE);
			System.out.println(Buffer.SAW);
			System.out.println(Buffer.SQUARE);
			System.out.println(Buffer.TRIANGLE);
			/*
			 * You an also store buffers statically in a
			 * Hashtable (but don't forget, and be careful not 
			 * to overwrite).
			 */
			Buffer myBuf = new Buffer(512);
			Buffer.staticBufs.put("MyBuf", myBuf);
			
		}
	}
	
	public static class BufferWithWavePlayer {
		public static void main(String[] args) {
			
		}
	}
}
