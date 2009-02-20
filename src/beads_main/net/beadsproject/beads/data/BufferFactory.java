package net.beadsproject.beads.data;


public abstract class BufferFactory {
	
	public static final int DEFAULT_BUFFER_SIZE = 512;
	
	public Buffer generateBuffer(int bufferSize) {
		return null;
	}
	
	public String getName() {
		return null;
	}
	
	public final Buffer getDefault() {
		String name = getName();
    	if(!Buffer.staticBufs.containsKey(name)) {
        	Buffer.staticBufs.put(name, generateBuffer(DEFAULT_BUFFER_SIZE));
    	}
    	return Buffer.staticBufs.get(name);
	}

}
