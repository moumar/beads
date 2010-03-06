package net.beadsproject.beads.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;

import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.TimeStamp;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.sample.SamplePlayer;
import net.beadsproject.beads.ugens.utility.DelayTrigger;
import net.beadsproject.beads.ugens.utility.Recorder;

/**
 * Segments a song, then reorders the segments from longest to shortest...
 * 
 * @author ben
 */
public class SongSegmenter {
	final static String inputFile = "D:/Music/Burnt Friedman & The Nu Dub Players/Just Landed/06 Railway Palace, Melbourne.mp3";
	final static String outputFile = "D:/tmp/ordered.wav";
	
	final static float lengthMs = 100000; // maximum length of outputFile

	static class Segment implements Comparable {
		public double start, end;

		public Segment(double s, double e) {
			start = s;
			end = e;
		}

		public double size() {
			return end - start;
		}

		public int compareTo(Object o) {
			if (size() < ((Segment) o).size())
				return -1;
			else
				return 1;
		}
	};

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Sample outputSample = null;

		System.out.println("Processing file: " + inputFile);

		try {
			Sample sample = new Sample(inputFile);
			List<Segment> segments = getAllSegments(sample);
			// sort segments by size
			Collections.sort(segments);
			int i = 0;
			int length = segments.size();

			AudioContext ac = new AudioContext();
			SamplePlayer sp = new SamplePlayer(ac, sample);
			sp.setKillOnEnd(false);
			sp.start();

			for (Segment s : segments) {
				System.out.printf("adding segment %d of %d (size %fms)\n", i,
						length, s.size());
				i++;

				if (outputSample == null) {
					outputSample = new Sample(ac.getAudioFormat(), 100);
					outputSample.clear();
				}

				Recorder rec = new Recorder(ac, outputSample,
						Recorder.Mode.INFINITE);
				rec.setPosition(outputSample.getLength());
				ac.out.addDependent(new DelayTrigger(ac, s.size(),
						new AudioContextStopTrigger(ac)));
				sp.setPosition(s.start);
				rec.addInput(sp);
				ac.out.addDependent(rec);
				ac.runNonRealTime();
				ac.out.clearInputConnections();
				rec.clip();

				if (outputSample != null && outputSample.getLength() > lengthMs)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (outputSample != null) {
			outputSample.write(outputFile, AudioFileFormat.Type.WAVE);
		} else
			System.out.println("Sorry, could not process that file!");
	}

	public static class SegmentRecorder implements SegmentListener {
		AudioContext ac;
		public List<Segment> segments;

		public SegmentRecorder(AudioContext ac) {
			this.ac = ac;
			segments = new ArrayList<Segment>();
		}

		public void newSegment(TimeStamp start, TimeStamp end) {
			segments.add(new Segment(start.getTimeMS(), end.getTimeMS()));
		}
	}

	/**
	 * Looks for a segment (bounded by onsets) in the audio file, and returns it
	 * in a Sample.
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static List<Segment> getAllSegments(Sample sample) throws Exception {
		AudioContext ac = new AudioContext();

		AudioAnalyser analyser = new AudioAnalyser(ac);
		ac.out.addDependent(analyser);
		PeakDetector pd = analyser.peakDetector();
		pd.setThreshold(0.1f);
		pd.setAlpha(.8f);

		Sample audioSample = sample; // new Sample(filename,
										// Sample.Regime.newStreamingRegime(100));
		if (audioSample == null)
			return null;
		float lengthMs = audioSample.getLength();
		assert lengthMs > 0;

		// skip about halfway, then locate a longish segment ...
		SamplePlayer sp = new SamplePlayer(ac, audioSample);
		sp.setPosition(Math.random() * lengthMs);
		SegmentRecorder sr = new SegmentRecorder(ac);
		pd.addSegmentListener(sr);

		analyser.addInput(sp);
		sp.setKillListener(new AudioContextStopTrigger(ac));
		ac.runNonRealTime();
		ac.out.clearInputConnections();

		return sr.segments;
	}

	static public Sample extractSegment(String s, Segment seg) throws Exception {
		AudioContext ac = new AudioContext();

		double start = seg.start;
		double end = seg.end;

		Sample output = new Sample(ac.getAudioFormat(), end - start);
		Recorder r = new Recorder(ac, output);
		r.setMode(Recorder.Mode.FINITE);
		ac.out.addDependent(r);
		SamplePlayer sp = new SamplePlayer(ac, new Sample(s));
		sp.setPosition(start);
		sp.start();
		r.addInput(sp);

		r.setKillListener(new AudioContextStopTrigger(ac));
		ac.runNonRealTime();
		r.clip();
		return output;
	}

	public static String[] allFilesInDir(String dir) {
		File directory = new File(dir);
		String[] fileNameList = directory.list();
		for (int i = 0; i < fileNameList.length; i++) {
			fileNameList[i] = directory.getAbsolutePath() + "/"
					+ fileNameList[i];
		}
		return fileNameList;
	}
}
