package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;
import java.util.List;

public class Opj2ProcessRunnerImp implements Opj2ProcessRunner {

	private static final int TIMEOUT_IN_MINUTES = 30;
	private final List<String> operations;

	public Opj2ProcessRunnerImp(List<String> operations) {
		this.operations = operations;
	}

	@Override
	public void runOpj2Process() throws OpenJpeg2Exception, InterruptedException, IOException {
		ProcessBuilder builder = new ProcessBuilder(operations);
		builder.inheritIO(); // send logs to console, use redirect.... to send elsewhere (file etc)
		try {
			Process process = builder.start();
			waitForConvertingToFinish(process);
		} catch (IOException e) {
			throw new IOException("IOException occured, openjpeg2-tools possibly not installed?");
		}
	}

	private void waitForConvertingToFinish(Process process)
			throws OpenJpeg2Exception, InterruptedException {
		long startTime = System.currentTimeMillis();
		long timeOutTime = getTimeOutTime(startTime);
		int exitCode = -1;
		while (waitingForProcessToFinish(timeOutTime, exitCode)) {
			try {
				exitCode = process.exitValue();
			} catch (IllegalThreadStateException e) {
				try {
					// Process is still working and active
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new InterruptedException("Failed to sleep during openjpeg2 conversion");
				}
			}
		}

		if (exitCode == -1) {
			process.destroy();
			throw OpenJpeg2Exception
					.withMessage("Converting image using openjpeg2 failed or timed out");
		}
	}

	private long getTimeOutTime(long startTime) {
		return startTime + TIMEOUT_IN_MINUTES * 60 * 1000;
	}

	private boolean waitingForProcessToFinish(long timeOutTime, int exitCode) {
		return System.currentTimeMillis() < timeOutTime && exitCode == -1;
	}
}
