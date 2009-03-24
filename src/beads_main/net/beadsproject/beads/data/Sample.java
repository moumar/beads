/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.beadsproject.beads.core.AudioUtils;

/**
 * A Sample is a multi-channel buffer of audio data which can be read from a file or
 * recorded from a UGen and written to a file.
 * 
 * @see SampleManager Recorder
 * 
 * @author ollie
 */
public class Sample {
    
	/** The file name of the Sample. */
	private String fileName;
	
    /** The audio format. */
    public final AudioFormat audioFormat;
    
    /** The number of channels. */
    public final int nChannels;
    
    /** The number of sample frames. */
    public final long nFrames;
    
    /** The length in milliseconds. */
    public final float length;
    
    /** The buffer containing the audio data. The data is stored in the form float[{@link #nChannels}][{@link #nFrames}] */
    public final float[][] buf;
    
    /**
     * Instantiates a new empty buffer with the specified audio format and
     * number of frames.
     * 
     * @param audioFormat the audio format.
     * @param totalFrames the number of sample frames.
     */
    public Sample(AudioFormat audioFormat, long totalFrames) {
        this.audioFormat = audioFormat;
        nChannels = audioFormat.getChannels();
        this.nFrames = totalFrames;
        buf = new float[nChannels][(int)totalFrames]; //TODO
        for(int i = 0; i < nChannels; i++) {
            for(int j = 0; j < totalFrames; j++) {
                buf[i][j] = 0.0f;
            }
        }
        length = totalFrames / audioFormat.getSampleRate() * 1000f;
    }

    /**
     * Creates a new Sample from the specified file.
     * 
     * <p/>In many cases it is preferable to use {@link SampleManager#sample(String)}, which adds the data to a static repository once it is loaded. 
     * 
     * @param fn the file path.
     * 
     * @throws UnsupportedAudioFileException Signals that the file format is not supported.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Sample(String fn) throws UnsupportedAudioFileException, IOException {
    	this.fileName = fn;
    	AudioInputStream audioInputStream = null;
    	try {    		
    		File fileIn = new File(fn);
    		if (fileIn.exists())
    			audioInputStream = AudioSystem.getAudioInputStream(fileIn);
    		else
    			audioInputStream = AudioSystem.getAudioInputStream((new URL(fn)).openStream());
    	} catch(Exception e) {
    		throw(new IOException("Cannot find file \"" + fn + "\". It either doesn't exist at the specified location or the URL is malformed."));    		    		
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
        length = nFrames / audioFormat.getSampleRate() * 1000f;
        System.out.println("loaded sample " + this + " with length " + length + "ms.");
    } 
    
    /**
     * Deinterleaves an interleaved array.
     * 
     * @param source the interleaved array.
     * 
     * @return the de-interleaved array (channels x frames).
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
     * @param source the source array.
     * 
     * @return the interleaved array (frame by frame, alternating channels).
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
     * Converts from milliseconds to samples based on the sample rate specified by {@link #audioFormat}.
     * 
     * @param msTime the time in milliseconds.
     * 
     * @return the time in samples.
     */
    public double msToSamples(double msTime) {
        return msTime * audioFormat.getSampleRate() / 1000.0f;
    }

    /**
     * Converts from samples to milliseconds based on the sample rate specified by {@link #audioFormat}.
     * 
     * @param sampleTime the time in samples.
     * 
     * @return the time in milliseconds.
     */
    public double samplesToMs(double sampleTime) {
        return sampleTime / audioFormat.getSampleRate() * 1000.0f;
    }
    
    /**
     * Retrieves a frame of audio using linear interpolation.
     * 
     * @param currentSample the current sample.
     * @param fractionOffset the offset from the current sample as a fraction of the time
     * to the next sample.
     * 
     * @return the interpolated frame.
     */
    public float[] getFrameLinear(int currentSample, float fractionOffset) {
        float[] result = new float[nChannels];
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
     * @param currentSample the current sample.
     * @param fractionOffset the offset from the current sample as a fraction of the time
     * to the next sample.
     * 
     * @return the interpolated frame.
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
     * Prints audio format info to System.out.
     */
    public void printAudioFormatInfo() {
        System.out.println("Sample Rate: " + audioFormat.getSampleRate());
        System.out.println("Channels: " + nChannels);
        System.out.println("Frame size in Bytes: " + audioFormat.getFrameSize());
        System.out.println("Encoding: " + audioFormat.getEncoding());
        System.out.println("Big Endian: " + audioFormat.isBigEndian());
    }   
    
    /**
     * Write Sample to a file.
     * 
     * @param fn the file name.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
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
    
    
    /**
     * Gets the full file name.
     * 
     * @return the file name
     */
    public String getFileName() {
    	return fileName;
    }
    

    /**
     * Gets the simple file name.
     * 
     * @return the file name
     */
    public String getSimpleFileName() {
    	return new File(fileName).getName();
    }
    
}
