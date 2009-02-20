
package net.beadsproject.beads.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.beadsproject.beads.analysis.FeatureFrame;
import net.beadsproject.beads.analysis.FeatureLayer;
import net.beadsproject.beads.analysis.SampleAnalyser;
import net.beadsproject.beads.analysis.featureextractors.Frequency;
import net.beadsproject.beads.analysis.featureextractors.MelSpectrum;
import net.beadsproject.beads.analysis.featureextractors.Power;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.segmenters.SimplePowerOnsetDetector;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.SamplePlayer;

// TODO: Auto-generated Javadoc
/**
 * A Sample is a buffer for audio data which can be loaded from a file or
 * recorded into the buffer.
 * 
 * @see SampleManager
 */
public class Sample {
    
	/** The name. */
	private String fileName;
	
    /** The audio format. */
    public final AudioFormat audioFormat;
    
    /** The number of channels. */
    public final int nChannels;
    
    /** The number of frames. */
    public final long nFrames;
    
    /** The length. */
    public final float length;
    
    /** The audio data. */
    public final float[][] buf;
    
    /** The result. */
    private float[] result;
    
    /**
	 * Instantiates a new empty buffer with the specified audio format and
	 * number of frames.
	 * 
	 * @param audioFormat
	 *            the audio format
	 * @param totalFrames
	 *            the number frames
	 */
    public Sample(AudioFormat audioFormat, long totalFrames) {
        this.audioFormat = audioFormat;
        nChannels = audioFormat.getChannels();
        this.nFrames = totalFrames;
        buf = new float[nChannels][(int)totalFrames]; //??
        for(int i = 0; i < nChannels; i++) {
            for(int j = 0; j < totalFrames; j++) {
                buf[i][j] = 0.0f;
            }
        }
        result = new float[nChannels];
        length = totalFrames / audioFormat.getSampleRate() * 1000f;
    }

    /**
	 * Creates a new Sample from the specified file.
	 * 
	 * @param fn
	 *            the file name
	 * 
	 * @throws UnsupportedAudioFileException
	 *             the unsupported audio file exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
    public Sample(String fn) throws UnsupportedAudioFileException, IOException {
    	this.fileName = fn;
    	AudioInputStream audioInputStream = null;
    	try {
    		File fileIn = new File(fn);
        	audioInputStream = AudioSystem.getAudioInputStream(fileIn);
    	} catch(Exception e) {
    		URL url = new URL(fn);
        	audioInputStream = AudioSystem.getAudioInputStream(url.openStream());
    	}
        audioFormat = audioInputStream.getFormat();
        nChannels = audioFormat.getChannels();
        nFrames = (int)audioInputStream.getFrameLength();
        if(nFrames == -1) throw new IOException();
        int frameHop = 256;
        byte[] audioBytes = new byte[frameHop * audioFormat.getFrameSize()];
        buf = new float[nChannels][(int)nFrames]; //??
        int framesRead = 0;
        int bytesRead;
		while ((bytesRead = audioInputStream.read(audioBytes)) != -1) {
			frameHop = bytesRead / audioFormat.getFrameSize();
            float[] bufTemp = new float[frameHop * nChannels];
            try {
            	//TODO - only set for 16 bit audio at the moment
            	AudioUtils.byteToFloat(bufTemp, audioBytes, audioFormat.isBigEndian());     
            } catch(Exception e) {
            	System.out.println("Problem loading audio file " + fn + ". Byes read this frame: " + bytesRead + ". Frames read: " + framesRead);
            	System.out.println("Is it 16-bit?");
            	e.printStackTrace();
            	break;
            }
            float[][] bufSegment = deinterleave(bufTemp);
            for (int i = 0; i < bufSegment.length; i++) {
				for (int j = 0; j < bufSegment[i].length; j++) {
					buf[i][j + framesRead] = bufSegment[i][j];	
				}
			}
            framesRead += frameHop;
        }
        audioInputStream.close();
        result = new float[nChannels];
        length = nFrames / audioFormat.getSampleRate() * 1000f;
        System.out.println("loaded sample " + this + " with length " + length + "ms.");
    } 
    
    /**
	 * Deinterleaves an interleaved array.
	 * 
	 * @param source
	 *            the interleaved array
	 * 
	 * @return the de-interleaved array (channels x frames)
	 */
    private float[][] deinterleave(float[] source) {
    	int nFrames = source.length / nChannels;
        float[][] result = new float[nChannels][nFrames];   
        for(int i = 0, count = 0; i < nFrames; i++) {
            for(int j = 0; j < nChannels; j++) {
                result[j][i] = source[count++];
            }
        }
        return result;
    }
    
    /**
	 * Interleaves a de-interleaved (channels x frames) array.
	 * 
	 * @param source
	 *            the source array
	 * 
	 * @return the interleaved array (frame by frame, alternating channels)
	 */
    private float[] interleave(float[][] source) {
        float[] result = new float[nChannels * (int)nFrames]; //??
        for(int i = 0, counter = 0; i < nFrames; i++) {
            for(int j = 0; j < nChannels; j++) {
                result[counter++] = source[j][i];
            }   
        }
        return result;
    }
    
    /**
	 * Converts from millieconds to samples.
	 * 
	 * @param msTime
	 *            the time in milliseconds
	 * 
	 * @return the time in samples
	 */
    public float msToSamples(float msTime) {
        return msTime * audioFormat.getSampleRate() / 1000.0f;
    }

    /**
	 * Converts from samples to milliseconds.
	 * 
	 * @param sampleTime
	 *            the time in samples
	 * 
	 * @return the time in milliseconds
	 */
    public float samplesToMs(float sampleTime) {
        return sampleTime / audioFormat.getSampleRate() * 1000.0f;
    }
    
    /**
	 * Retrieves a frame of audio using linear interpolation.
	 * 
	 * @param currentSample
	 *            the current sample
	 * @param fractionOffset
	 *            the offset from the current sample as a fraction of the time
	 *            to the next sample
	 * 
	 * @return the interpolated frame
	 */
    public float[] getFrameLinear(int currentSample, float fractionOffset) {
        if(currentSample >= 0 && currentSample < nFrames) {
            for (int i = 0; i < nChannels; i++) {
                if(currentSample < (nFrames - 1)) {
                    result[i] = (1f - fractionOffset) * buf[i][currentSample] +
                            fractionOffset * buf[i][currentSample + 1];
                } else {
                    result[i] = buf[i][currentSample];
                }   
            }
        } else {
             for(int i = 0; i < nChannels; i++) {
                 result[i] = 0.0f;
             }
        }
        return result;
    }
    
    /**
	 * Retrieves a frame of audio using cubic interpolation.
	 * 
	 * @param currentSample
	 *            the current sample
	 * @param fractionOffset
	 *            the offset from the current sample as a fraction of the time
	 *            to the next sample
	 * 
	 * @return the interpolated frame
	 */
    public float[] getFrameCubic(int currentSample, float fractionOffset) {
        float[] result = new float[nChannels];
        float a0,a1,a2,a3,mu2;
        float ym1,y0,y1,y2;
        for (int i = 0; i < nChannels; i++) {
            int realCurrentSample = currentSample;
            if(realCurrentSample >= 0 && realCurrentSample < (nFrames - 1)) {
                realCurrentSample--;
                if (realCurrentSample < 0) {
                    ym1 = buf[i][0];
                    realCurrentSample = 0;
                } else {
                    ym1 = buf[i][realCurrentSample++];
                }
                y0 = buf[i][realCurrentSample++];
                if (realCurrentSample >= nFrames) {
                    y1 = buf[i][(int)nFrames - 1]; //??
                } else {
                    y1 = buf[i][realCurrentSample++];
                }
                if (realCurrentSample >= nFrames) {
                    y2 = buf[i][(int)nFrames - 1]; //??
                } else {
                    y2 = buf[i][realCurrentSample];
                }
                mu2 = fractionOffset * fractionOffset;
                a0 = y2 - y1 - ym1 + y0;
                a1 = ym1 - y0 - a0;
                a2 = y1 - ym1;
                a3 = y0;
                result[i] = a0 * fractionOffset * mu2 + a1 * mu2 + a2 * fractionOffset + a3;
            } else {
                result[i] = 0.0f;
            }
        }
        return result;
    }

    /**
	 * Post audio format info.
	 */
    public void postAudioFormatInfo() {
        System.out.println("Sample Rate: " + audioFormat.getSampleRate());
        System.out.println("Channels: " + nChannels);
        System.out.println("Frame size in Bytes: " + audioFormat.getFrameSize());
        System.out.println("Encoding: " + audioFormat.getEncoding());
        System.out.println("Big Endian: " + audioFormat.isBigEndian());
    }   
    
    /**
	 * Write to a file.
	 * 
	 * @param fn
	 *            the file name
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
    public void write(String fn) throws IOException {
        byte[] bytes = new byte[(int)nFrames * audioFormat.getFrameSize()]; //??
        AudioUtils.floatToByte(bytes, interleave(buf), audioFormat.isBigEndian());
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        AudioInputStream aos = new AudioInputStream(bais, audioFormat, nFrames);
        AudioSystem.write(aos, AudioFileFormat.Type.AIFF, new File(fn));
        fileName = fn;
    }

    

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return fileName;
    }
    
    public String getFileName() {
    	return fileName;
    }
    
    /**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @throws Exception
	 *             the exception
	 */
    public static void main(String[] args) throws Exception {
    	Sample s1 = new Sample("/Users/ollie/Music/Audio/output5.aif");	
//    	Sample s1 = new Sample("audio/1234.aif");	
//    	s1.loadFeatures();
    }
    
}
