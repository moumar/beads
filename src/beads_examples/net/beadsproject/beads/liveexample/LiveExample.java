package net.beadsproject.beads.liveexample;

import net.beadsproject.beads.gui.BeadsGui;
import net.beadsproject.beads.play.Environment;
import net.beadsproject.beads.play.SongGroup;

public class LiveExample {
	
	public static void main(String[] args) {
		Environment env = null;
		try {
			env = Environment.loadEnvironment("com.olliebown.beads.play.DefaultEnvironmentFactory");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		BeadsGui bgu = new BeadsGui(env);
		bgu.addSongGroup(new SongGroup("g1"));
		bgu.addSongPart(new SongPart1("p1", env));
		bgu.addSongPart(new SongPart2("p2", env));
//		((Clock)env.elements.get("master clock")).setClick(true);
	}
}
