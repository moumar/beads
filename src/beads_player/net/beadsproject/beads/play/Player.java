package net.beadsproject.beads.play;

import java.util.ArrayList;
import java.util.List;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;

/**
 * The Class Player. Handles playing of groups. Does not know about GUI stuff.
 */
public class Player {

	private SongGroup currentGroup = null;
	private SongGroup nextGroup;
	private ArrayList<SongPart> playingParts;
	
	public Player(Environment e) {
		playingParts = new ArrayList<SongPart>();
		//I'm worried that if SongParts are also listening to this clock
		//they might miss out on the first 'beat'.
		//I think this is OK as long as the Player is the first thing to listen to the clock.
		//if it is then it will switch on the SongParts, and then immediately after that
		//the clock will trigger them.
		//However, it will be a problem if one were to change environments after initial startup.
		e.pathways.get("master clock").add(new Bead() {
			public void messageReceived(Bead message) {
				if(nextGroup != null) {
					System.out.println("tick");
					if(((Clock)message).getCount() % nextGroup.getFlipQuantisation() == 0) {
						doPlayGroupNow(nextGroup);
					}
				}
			}
		});
	}
	
	public void playGroup(SongGroup newGroup) {
		if(newGroup.getFlipQuantisation() < 1) {
			doPlayGroupNow(newGroup);
		} else {
			nextGroup = newGroup;
			System.out.println("timed start");
		}
	}
	
	private void doPlayGroupNow(SongGroup newGroup) {
		if(currentGroup != null) {
			ArrayList<SongPart> incoming = new ArrayList<SongPart>();
			ArrayList<SongPart> outgoing = new ArrayList<SongPart>();
			for(SongPart p : currentGroup.parts()) {
				if(!newGroup.parts().contains(p)) {
					outgoing.add(p);
				}
			}
			for(SongPart p : newGroup.parts()) {
				if(!currentGroup.parts().contains(p)) {
					incoming.add(p);
				}
			}
			endPlayingList(outgoing);
			beginPlayingList(incoming);
		} else {
			beginPlayingList(newGroup.parts());
		}
		currentGroup = newGroup;
		nextGroup = null;
	}

	public void stop() {
		endPlayingList(currentGroup.parts());
		currentGroup = null;
	}
	
	private void beginPlayingList(List<SongPart> list) {
		for(SongPart p : list) {
			beginPlayingPart(p);
		}
	}
	
	private void beginPlayingPart(SongPart p) {
		p.enter();
		p.pause(false);
		playingParts.add(p);
	}
	
	private void endPlayingList(List<SongPart> list) {
		for(SongPart p : list) {
			endPlayingPart(p);
		}
	}
	
	private void endPlayingPart(SongPart p) {
		p.exit();
		playingParts.remove(p);
	}
	
	public SongGroup getCurrentGroup() {
		return currentGroup;
	}

	public void setCurrentGroup(SongGroup currentGroup) {
		this.currentGroup = currentGroup;
	}
		
	public void notifyCurrentGroupUpdated() {
		if(currentGroup != null) {
			for(SongPart p : currentGroup.parts()) {
				if(!playingParts.contains(p)) {
					beginPlayingPart(p);
				}
			}
			for(SongPart p : (List<SongPart>)playingParts.clone()) {
				if(!currentGroup.contains(p)) {
					endPlayingPart(p);
				}
			}
		}
	}

}
