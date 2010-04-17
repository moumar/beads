package net.beadsproject.beads.core.io;
import net.beadsproject.beads.core.AudioContext;


public class MyBeadsPlugin extends BeadsPlugin {

	public MyBeadsPlugin(long Wrapper) {
		super(Wrapper);
	}

	@Override
	public void setup(AudioContext ac) {
		// TODO Auto-generated method stub

	}

	@Override
	public int canDo(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPlugCategory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getProductString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProgramNameIndexed(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVendorString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setBypass(boolean arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean string2Parameter(int arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumParams() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumPrograms() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getParameter(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getParameterDisplay(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameterLabel(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameterName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getProgram() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getProgramName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameter(int arg0, float arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProgram(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProgramName(String arg0) {
		// TODO Auto-generated method stub

	}

}
