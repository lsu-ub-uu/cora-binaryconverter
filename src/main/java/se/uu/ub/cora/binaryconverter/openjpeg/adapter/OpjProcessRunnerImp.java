/*
 * Copyright 2023 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.binaryconverter.openjpeg.adapter;

public class OpjProcessRunnerImp implements OpjProcessRunner {

	private static final int TO_SECOND = 1000;
	private final OpjProcessBuilder builder;
	private int pollSleepTime;
	private long timeout;
	private int timeoutInSeconds;

	public OpjProcessRunnerImp(OpjProcessBuilder builder, int pollSleepTimeInMillisecond,
			int timeoutInSeconds) {
		this.builder = builder;
		this.pollSleepTime = pollSleepTimeInMillisecond;
		this.timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public void runOpjProcess() {
		Process process = builder.start();
		timeout = calculateTimeout(timeoutInSeconds);
		waitForConvertingToFinish(process);
	}

	private long calculateTimeout(int timeoutInSeconds) {
		return System.currentTimeMillis() + timeoutInSeconds * TO_SECOND;
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
		return checkTimeout(System.currentTimeMillis(), timeout) && exitCode != 0;
	}

	boolean checkTimeout(long currentTimeMillis, long timeout) {
		return currentTimeMillis <= timeout;
	}

	void sleep() {
		try {
			threadSleep();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw OpenJpegException.withMessageAndException(
					"Converting image using openjpeg2 failed or timed out", e);
		}
	}

	void threadSleep() throws InterruptedException {
		Thread.sleep(pollSleepTime);
	}

	private void throwExceptionWhenProcessNotSuccessful(Process process, int exitCode) {
		if (exitCode != 0) {
			destroyProcess(process);
			throw OpenJpegException
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

	public OpjProcessBuilder onlyForTestGetProcessBuilder() {
		return builder;
	}

	public Object onlyForTestGetPollSleepTimeInMilliseconds() {
		return pollSleepTime;
	}

	public Object onlyForTestGetTimeoutInSeconds() {
		return timeoutInSeconds;
	}
}
