package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;

public class Opj2ProcessRunnerImp implements Opj2ProcessRunner {

	private static final int TIMEOUT_IN_MINUTES = 30;
	private final ProcessBuilder builder;

	public Opj2ProcessRunnerImp(ProcessBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void runOpj2Process() throws OpenJpeg2Exception, InterruptedException, IOException {
		try {
			Process process = builder.start();
			waitForConvertingToFinish(process);
		} catch (IOException e) {
			throw new IOException("IOException occured, openjpeg2-tools possibly not installed?");
		}
	}

	private void waitForConvertingToFinish(Process process)
			throws OpenJpeg2Exception, InterruptedException {
		long timeOutTime = getTimeOutTime();
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

	private long getTimeOutTime() {
		return System.currentTimeMillis() + TIMEOUT_IN_MINUTES * 60 * 1000;
	}

	private boolean waitingForProcessToFinish(long timeOutTime, int exitCode) {
		return System.currentTimeMillis() < timeOutTime && exitCode == -1;
	}
}
