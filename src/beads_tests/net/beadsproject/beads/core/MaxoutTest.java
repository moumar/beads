package net.beadsproject.beads.core;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class MaxoutTest {

	public static int sleepTime = 1000;
	
	public static void main(String[] args) {
		//set up a simple audio chain
		AudioContext ac = new AudioContext();
		Gain g = new Gain(ac, 1, 0.1f);
		WavePlayer wp = new WavePlayer(ac, 500f, new SineBuffer().getDefault());
		g.addInput(wp);
		ac.out.addInput(g);
		ac.start();

		JFrame jf = new JFrame();
		JSlider js = new JSlider(0, 1000, 1000);
		js.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sleepTime = ((JSlider)e.getSource()).getValue();
			}
		});
		jf.add(js);
		jf.pack();
		jf.setVisible(true);
		
		Thread t = new Thread() {
			public void run() {
				while(true) {
					try {
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						System.out.println(Math.pow(Math.random(), Math.random()));
						sleep(sleepTime);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		
	}
}
