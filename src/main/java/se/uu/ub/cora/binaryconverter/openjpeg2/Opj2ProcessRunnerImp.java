package se.uu.ub.cora.binaryconverter.openjpeg2;

public class Opj2ProcessRunnerImp implements Opj2ProcessRunner {

	private final Opj2ProcessBuilder builder;
	private int pollSleepTime;
	private long timeout;
	private int timeoutInSeconds;

	public Opj2ProcessRunnerImp(Opj2ProcessBuilder builder, int pollSleepTimeInMillisecond,
			int timeoutInSeconds) {
		this.builder = builder;
		this.pollSleepTime = pollSleepTimeInMillisecond;
		this.timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public void runOpj2Process() {
		Process process = builder.start();
		timeout = calculateTimeout(timeoutInSeconds);
		waitForConvertingToFinish(process);
	}

	private long calculateTimeout(int timeoutInSeconds) {
		return System.currentTimeMillis() + timeoutInSeconds * 1000;
	}

	private void waitForConvertingToFinish(Process process) {
		int exitCode = -1;
		exitCode = pollProcess(process, exitCode);
		throwExceptionWhenProcessNotSuccessful(process, exitCode);
	}

	private int pollProcess(Process process, int exitCode) {
		while (waitingForProcessToFinish(exitCode)) {
			try {
				exitCode = process.exitValue();
			} catch (IllegalThreadStateException e) {
				sleep();
			}
		}
		return exitCode;
	}

	private boolean waitingForProcessToFinish(int exitCode) {
		return System.currentTimeMillis() < timeout && exitCode != 0;
	}

	private void sleep() {
		try {
			Thread.sleep(pollSleepTime);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void throwExceptionWhenProcessNotSuccessful(Process process, int exitCode) {
		if (exitCode != 0) {
			destroyProcess(process);
			throw OpenJpeg2Exception
					.withMessage("Converting image using openjpeg2 failed or timed out");
		}
	}

	private void destroyProcess(Process process) {
		process.destroy();
		sleep();
		if (process.isAlive()) {
			process.destroyForcibly();
		}
	}
}
