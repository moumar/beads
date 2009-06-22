package net.beadsproject.beads.ugens;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;

/**
 * Smashes the system by adding and removing many ugens ...
 * 
 * Expect it to crash when it runs out of memory.
 * 
 */
public class UGenStressTest {

	static public int NUM = 10;
	static public AudioContext ac;
	static public PolyLimit pl;
	static public ThreadPoolExecutor executor;

	static void sine(int i, int j) {
		WavePlayer wp = new WavePlayer(ac, 440.f + 440.f * (1.0f * i / NUM),
				Buffer.SINE);
		Envelope gainEnv = new Envelope(ac, 0);
		Gain g = new Gain(ac, 1, gainEnv);
		g.addInput(wp);
		PanMonoToStereo pms = new PanMonoToStereo(ac);
		pms.getPanEnvelope().setValue(1.f * j / NUM);
		pms.addInput(g);
		gainEnv.addSegment(2.f / (NUM * NUM), 1);
		gainEnv.addSegment(1.5f / (NUM * NUM), 10);
		gainEnv.addSegment(0f, 100, new KillTrigger(pms));
		pl.addInput(pms);
	}

	public static void main(String[] args) {
		ac = new AudioContext();
		pl = new PolyLimit(ac, 2, 10000);
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

		final Clock c = new Clock(ac, 10);
		ac.out.addDependent(c);
		c.addMessageListener(new Bead() {
			int ticks = 1;

			public void messageReceived(Bead message) {
				if (((Clock) message).isBeat()) {
					for (int i = 0; i < ticks; i++) {
						executor.execute(new Runnable() {
							public void run() {
								sine((int) (Math.random() * NUM), (int) (Math
										.random() * NUM));
							}
						});
					}
					ticks *= 2;					
				}
			}
		});

		ac.out.addInput(pl);
		ac.out.setValue(1f);
		ac.start();
	}

}
