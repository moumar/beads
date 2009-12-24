package net.beadsproject.beads.ugens;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * A Quadrophonic mixer. Add sources with locations and change the locations. Locations are changed on a per-channel basis,
 * so that multichannel files can be located in the quadrophonic mixer independently. 
 * 
 * @author ollie
 *
 */
public class QuadrophonicMixer extends UGen {

	private class Location {
		
		UGen source;
		UGen[][] pos; 	//pos[chan][dim] - for each output channel of the source, 
					   	//the position is given by 2 Glides (x, y).
		boolean ownsPosition;
		
		Location(UGen source) {
			this.source = source;
			pos = new UGen[source.getOuts()][2];
			for(int i = 0; i < pos.length; i++) {
				for(int j = 0; j < 2; j++) {
					pos[i][j] = new Glide(context, 100f); //the position is set to be far-far away
				}
			}		
			ownsPosition = true;
		}
		
		Location(UGen source, UGen[][] controllers) {
			this.source = source;
			this.pos = controllers;
			ownsPosition = false;
		}
		
		void move(int channel, float[] newPos) {
			if(!ownsPosition) return;
			for(int i = 0; i < pos[channel].length; i++) {
				pos[channel][i].setValue(newPos[i]);
			}
		}
		
		void moveImmediately(int channel, float[] newPos) {
			if(!ownsPosition) return;
			for(int i = 0; i < pos[channel].length; i++) {
				((Glide)pos[channel][i]).setValueImmediately(newPos[i]);
			}
		}
		
		void mixInAudio(float[][] output) {
			//update the source
			source.update();
			//for each channel
			for(int outputChannel = 0; outputChannel < pos.length; outputChannel++) {
				//first update the glides
				for(int dim = 0; dim < 2; dim++) {
					pos[outputChannel][dim].update();
				}
				//at each time step
				for(int time = 0; time < bufferSize; time++) {
					//get current position of this channel using pos[outputChannel][dim].getValue(0, time);
					float[] currentPos = new float[2];
					for(int dim = 0; dim < 2; dim++) {
						currentPos[dim] = pos[outputChannel][dim].getValue(0, time);
					}	
					float[] speakerGains = new float[4];
					//work out speaker gains given current pos (distance of pos from each speaker?)
					for(int speaker = 0; speaker < 4; speaker++) {
						float distance = distance(speakerPositions[speaker], currentPos);
						float linearGain = Math.max(0, 1f - distance / circleDiameter);
						speakerGains[speaker] = (float)Math.pow(linearGain, curve);
					}
					//then mix that channel in
					for(int speaker = 0; speaker < 4; speaker++) {
						output[speaker][time] += speakerGains[speaker] * source.getValue(outputChannel, time);
					}
				}
			}
		}
	}
	
	//default speaker numbering: layout speakers 1-4 on the ground in clockwise order
	//the y-axis follows the line joining 1 and 4
	//the x-axis follows the line joining 1 and 2
	public static float[][] speakerPositions;
	public static float circleDiameter;

	private Map<UGen, Location> sources;
	private float curve; //values over 1 will focus the sound on individual speakers more
								//values below 1 will spread the sound, with zero being closer to an equal mix
								//1 is linear
								//speakers will not play a sound that is further than 1 diameter away from them
	
	public QuadrophonicMixer(AudioContext context) {
		this(context, new float[][] {
				{0,0},
				{1,0},
				{1,1},
				{0,1}
				}, (float)Math.sqrt(2f));
	}
	
	public QuadrophonicMixer(AudioContext context, float[][] locations, float diameter) {
		super(context, locations.length);
		setSpeakerPositions(locations);
		setCircleDiameter(diameter);
		outputInitializationRegime = OutputInitializationRegime.ZERO;
		sources = Collections.synchronizedMap(new Hashtable<UGen, Location>());
		curve = 3f;
	}
	
	public void setCircleDiameter(float sd) {
		circleDiameter = sd;
	}
	
	public void setSpeakerPositions(float[][] locations) {
		speakerPositions = new float[locations.length][2];
		for(int i = 0; i < speakerPositions.length; i++) {
			for(int j = 0; j < 2; j++) {
				speakerPositions[i][j] = locations[i][j];
			}
		}
	}
	
	public static float distance(float[] a, float[] b) {
		float distance = 0;
		for(int i = 0; i < a.length; i++) {
			distance += (a[i] - b[i]) * (a[i] - b[i]);
		}
		distance = (float)Math.sqrt(distance);
		return distance;
	}
	
	public void addInput(UGen source) {
		Location location = new Location(source);
		sources.put(source, location);
	}
	
	public void addInput(UGen source, UGen[][] controllers) {
		Location location = new Location(source, controllers);
		sources.put(source, location);
	}
	
	public void setLocation(UGen source, int channel, float[] newPos) {
		sources.get(source).move(channel, newPos);
	}
	
	public void setLocationImmediately(UGen source, int channel, float[] newPos) {
		sources.get(source).moveImmediately(channel, newPos);
	}
	
	public void removeSource(UGen source) {
		sources.remove(source);
	}
	
	
	@Override
	public synchronized void clearInputConnections() {
		super.clearInputConnections();
		sources.clear();
	}

	@Override
	public synchronized void removeAllConnections(UGen sourceUGen) {
		super.removeAllConnections(sourceUGen);
		removeSource(sourceUGen);
	}

	public void setCurve(float curve) {
		this.curve = curve;
	}

	@Override
	public void calculateBuffer() {
		for(Location location : sources.values()) {
			location.mixInAudio(bufOut);
		}
	}


}
