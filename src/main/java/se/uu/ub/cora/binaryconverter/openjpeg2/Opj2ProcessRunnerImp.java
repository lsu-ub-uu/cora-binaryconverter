package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;
import java.util.LinkedList;

public class Opj2ProcessRunnerImp implements Opj2ProcessRunner {

	private static final int TIMEOUT_IN_MINUTES = 30;
	private final LinkedList<String> operations;

	public Opj2ProcessRunnerImp(LinkedList<String> operations) {
		this.operations = operations;
	}

	@Override
	public int convertImage() {
		ProcessBuilder builder = new ProcessBuilder(operations);
		builder.inheritIO(); // send logs to console, use redirect.... to send elsewhere (file etc)
		try {
			Process process = builder.start();
			return waitForConvertingToFinish(process);
		} catch (IOException e) {
			System.out.println("some failure, " + e);
			return -1;
		}
	}

	int waitForConvertingToFinish(Process process) {
		long startTime = System.currentTimeMillis();
		long timeOutTime = getTimeOut(startTime);
		int exitCode = -1;
		while (waitingForProcessToFinish(timeOutTime, exitCode)) {
			try {
				exitCode = process.exitValue();
			} catch (IllegalThreadStateException e) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}

		if (exitCode == -1) {
			System.out.println("Converting image failed or timed out");
			process.destroy();
		}

		return exitCode;
	}

	private long getTimeOut(long startTime) {
		return startTime + TIMEOUT_IN_MINUTES * 60 * 1000;
	}

	private boolean waitingForProcessToFinish(long timeOutTime, int exitCode) {
		return System.currentTimeMillis() < timeOutTime && exitCode == -1;
	}
}
