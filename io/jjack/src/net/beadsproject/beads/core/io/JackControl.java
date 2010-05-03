package net.beadsproject.beads.core.io;
import java.io.IOException;

public class JackControl {
	
	public static Process jackProcess;
	public static Thread jackShutdownHook = new Thread() {
		public void run() {
			stopJack();
		}
	};
	
	public static void startJack() {
		startJack("coreaudio", 2, 2);
	}

	public static void startJack(String driver, int inputs, int outputs) {
		if(jackProcess == null) {
			try {
				jackProcess = Runtime.getRuntime().exec(
						"/usr/local/bin/jackdmp --realtime -d " + driver + " -i " + inputs + " -o " + outputs + ""
						.split(" "));
				pipeJackOutput();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Runtime.getRuntime().addShutdownHook(jackShutdownHook);
	}
	
	public static void pipeJackOutput() {
		if(jackProcess == null) return;		
		//need to scan the output stream to see what happened
		Thread jackPipeThread = new Thread() {
			byte[] b = new byte[1000];
			public void run() {
				while(true) {
					try {
						int exitValue = jackProcess.exitValue();
						System.out.println("Jack terminated " + exitValue);
						break;
					} catch(IllegalThreadStateException e) {
						//this means jackProcess is running, so continue
					}
					try {
						int bytesRead = jackProcess.getInputStream().read(b);
						if(bytesRead >= 0) {
							System.out.write(b, 0, bytesRead);
						}
						bytesRead = jackProcess.getErrorStream().read(b);
						if(bytesRead >= 0) {
							System.err.write(b, 0, bytesRead);
						}
						sleep(100);
					} catch(Exception e) {
//						e.printStackTrace();	//ugly, ignore this
					}
				}
			}
		};
		jackPipeThread.start();
	}
	
	public static void stopJack() {
		if(jackProcess != null) {
			jackProcess.destroy();
		}
	}
}
