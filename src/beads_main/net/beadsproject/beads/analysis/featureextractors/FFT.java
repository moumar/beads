package net.beadsproject.beads.analysis.featureextractors;

import java.util.ArrayList;
import net.beadsproject.beads.analysis.FeatureExtractor;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.HanningWindow;
import net.beadsproject.beads.data.buffers.SineBuffer;
import net.beadsproject.beads.ugens.WavePlayer;

// TODO: Auto-generated Javadoc
/**
 * The Class FFT.
 */
public class FFT extends FeatureExtractor {
	
	protected ArrayList<FeatureExtractor> listeners;
	
	/** The fft real. */
	protected float[] fftReal;
	
	/** The fft imag. */
	protected float[] fftImag;
	
	public FFT() {
		listeners = new ArrayList<FeatureExtractor>();
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void process(float[] data, int length) {
		fft(data, length, true);
		fftReal = getReal(data, length);
		fftImag = getImag(data, length);
		for(FeatureExtractor fe : listeners) {
			fe.process(fftReal, length);
		}
	}
	
	/*
	 * All of the code below this line is taken from Holger Crysandt's MPEG7AudioEnc project.
	 * See http://mpeg7audioenc.sourceforge.net/copyright.html for license and copyright.
	 */
	
	/**
	 * Gets the real.
	 * 
	 * @param spectrum
	 *            Half complex spectrum
	 * @param length 
	 * 
	 * @return Returns real part of half complex spectrum
	 */
	public static float[] getReal(float[] spectrum, int length) {
		float[] real = new float[length];
		real[0] = spectrum[0];
		real[real.length/2] = spectrum[1];
		for (int i=1, j=real.length-1; i<j; ++i, --j)
			real[j] = real[i] = spectrum[2*i];
		return real;
	}
	
	/**
	 * Gets the imag.
	 * 
	 * @param spectrum
	 *            Half complex spectrum
	 * @param length 
	 * 
	 * @return Returns imaginary part of half complex spectrum
	 */
	public static float[] getImag(float[] spectrum, int length) {
		float[] imag = new float[length];
		for (int i=1, j=imag.length-1; i<j; ++i, --j)
		  imag[i] = -(imag[j] = spectrum[2*i+1]);
		return imag;
	}

	/**
	 * Fft.
	 * 
	 * @param data
	 *            the data
	 * @param length 
	 * @param isign
	 *            the isign
	 */
	public static void fft(float[] data, int n, boolean isign) {
		float c1 = 0.5f; 
		float c2, h1r, h1i, h2r, h2i;
		double wr, wi, wpr, wpi, wtemp;
		double theta = 3.141592653589793/(n>>1);
		if (isign) {
			c2 = -.5f;
			four1(data, n>>1, true);
		} else {
			c2 = .5f;
			theta = -theta;
		}
		wtemp = Math.sin(.5*theta);
		wpr = -2.*wtemp*wtemp;
		wpi = Math.sin(theta);
		wr = 1. + wpr;
		wi = wpi;
		int np3 = n + 3;
		for (int i=2,imax = n >> 2, i1, i2, i3, i4; i <= imax; ++i) {
			/** @todo this can be optimized */
			i4 = 1 + (i3 = np3 - (i2 = 1 + (i1 = i + i - 1)));
			--i4; --i2; --i3; --i1; 
			h1i =  c1*(data[i2] - data[i4]);
			h2r = -c2*(data[i2] + data[i4]);
			h1r =  c1*(data[i1] + data[i3]);
			h2i =  c2*(data[i1] - data[i3]);
			data[i1] = (float) ( h1r + wr*h2r - wi*h2i);
			data[i2] = (float) ( h1i + wr*h2i + wi*h2r);
			data[i3] = (float) ( h1r - wr*h2r + wi*h2i);
			data[i4] = (float) (-h1i + wr*h2i + wi*h2r);
			wr = (wtemp=wr)*wpr - wi*wpi + wr;
			wi = wi*wpr + wtemp*wpi + wi;
		}
		if (isign) {
			float tmp = data[0]; 
			data[0] += data[1];
			data[1] = tmp - data[1];
		} else {
			float tmp = data[0];
			data[0] = c1 * (tmp + data[1]);
			data[1] = c1 * (tmp - data[1]);
			four1(data, n>>1, false);
		}
	}
	
	/**
	 * Four1.
	 * 
	 * @param data
	 *            the data
	 * @param nn
	 *            the nn
	 * @param isign
	 *            the isign
	 */
	private static void four1(float data[], int nn, boolean isign) {
		int n, mmax, istep;
		double wtemp, wr, wpr, wpi, wi, theta;
		float tempr, tempi;
		
		n = nn << 1;				
		for (int i = 1, j = 1; i < n; i += 2) {
			if (j > i) {
				// SWAP(data[j], data[i]);
				float swap = data[j-1];
				data[j-1] = data[i-1];
				data[i-1] = swap;
				// SWAP(data[j+1], data[i+1]);
				swap = data[j];
				data[j] = data[i]; 
				data[i] = swap;
			}			
			int m = n >> 1;
			while (m >= 2 && j > m) {
				j -= m;
				m >>= 1;
			}
			j += m;
		}
		mmax = 2;
		while (n > mmax) {
			istep = mmax << 1;
			theta = 6.28318530717959 / mmax;
			if (!isign)
				theta = -theta;
			wtemp = Math.sin(0.5 * theta);
			wpr = -2.0 * wtemp * wtemp;
			wpi = Math.sin(theta);
			wr = 1.0;
			wi = 0.0;
			for (int m = 1; m < mmax; m += 2) {
				for (int i = m; i <= n; i += istep) {
					int j = i + mmax;
					tempr = (float) (wr * data[j-1] - wi * data[j]);	
					tempi = (float) (wr * data[j]   + wi * data[j-1]);	
					data[j-1] = data[i-1] - tempr;
					data[j]   = data[i] - tempi;
					data[i-1] += tempr;
					data[i]   += tempi;
				}
				wr = (wtemp = wr) * wpr - wi * wpi + wr;
				wi = wi * wpr + wtemp * wpi + wi;
			}
			mmax = istep;
		}
	}

	public void addListener(FeatureExtractor fe) {
		listeners.add(fe);
	}
	
	
}
