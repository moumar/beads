
package org.jaudiolibs.beads;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.UGen;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;
import org.jaudiolibs.audioservers.jack.JackAudioServer;
import org.jaudiolibs.audioservers.javasound.JavasoundAudioServer;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class AudioServerIO extends AudioIO implements AudioClient {
  
    private final String device;
    private final boolean jack;
    private AudioServer server;
    private AudioConfiguration config;   
    private List<FloatBuffer> inputs;

    private AudioServerIO(String device, boolean jack) {
        this.jack = jack;
        this.device = device;
    }

    @Override
    protected boolean start() {
        System.out.println("Starting AudioServerIO");
        config = new AudioConfiguration(
                context.getSampleRate(),
                context.getAudioFormat().inputs,
                context.getAudioFormat().outputs,
                context.getBufferSize(),
                true);
        if (jack) {
            System.out.println("-- JACK");
            server = JackAudioServer.create(device, config, false, this);
        } else {
            try {
                System.out.println("-- JavaSound");
                server = JavasoundAudioServer.create(device, config, JavasoundAudioServer.TimingMode.Estimated, this);
            } catch (Exception ex) {
                Logger.getLogger(AudioServerIO.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        Thread audioThread = new Thread(new Runnable() {

            public void run() {
                try {
                    server.run();
                } catch (Exception ex) {
                    Logger.getLogger(AudioServerIO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, "audio");
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();
        return true;
    }

    @Override
    protected UGen getAudioInput(int[] channels) {
        return new RTInput(context, channels);
    }

    public void configure(AudioConfiguration ac) throws Exception {
        if (config.getSampleRate() != ac.getSampleRate()
                || config.getInputChannelCount() != ac.getInputChannelCount()
                || config.getOutputChannelCount() != ac.getOutputChannelCount()
                || config.getMaxBufferSize() != ac.getMaxBufferSize()
                || !ac.isFixedBufferSize()) {
            System.out.println("Unexpected audio configuration");
            throw new IllegalArgumentException("Unexpected audio configuration");
        }
    }

    public boolean process(long time, List<FloatBuffer> inputs, List<FloatBuffer> outputs, int nFrames) {
        if (!context.isRunning()) {
            return false;
        }
        this.inputs = inputs;
        update();
        for (int i=0; i < outputs.size(); i++ ) {
            outputs.get(i).put(context.out.getOutBuffer(i));
        }
        this.inputs = null;
        return true;
    }

    public void shutdown() {
        // no op
    }
    
    private class RTInput extends UGen {

		private int[] channels;
		
		RTInput(AudioContext context, int[] channels) {
			super(context, channels.length);
			this.channels = channels;
		}

		@Override
		public void calculateBuffer() {
//			try {
				for (int i=0; i < channels.length; i++) {
					FloatBuffer fbuf = inputs.get(channels[i] - 1);		//OB correction to code here, assumption is that initialising channels array starts from 1, not 0.
		            fbuf.get(bufOut[i]);								//OB this line is reporting java.nio.BufferUnderflowException sometimes (not clear exactly when
		        }														//furthermore somewhere in the call chain these exceptions are getting snuffed
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
		}
		
	}
    
    public static AudioServerIO createJavaSoundIO() {
        return createJavaSoundIO("");
    }
    
    public static AudioServerIO createJavaSoundIO(String device) {
        if (device == null) {
            throw new NullPointerException();
        }
        return new AudioServerIO(device, false);
    }
    
    public static AudioServerIO createJackIO() {
        return createJackIO("Beads");
    }
    
    public static AudioServerIO createJackIO(String device) {
        if (device == null) {
            throw new NullPointerException();
        }
        return new AudioServerIO(device, true);
    }
    
}
