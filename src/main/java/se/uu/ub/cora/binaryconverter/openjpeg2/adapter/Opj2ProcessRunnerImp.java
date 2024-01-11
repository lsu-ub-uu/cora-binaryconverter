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
package se.uu.ub.cora.binaryconverter.openjpeg2.adapter;

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

	public Opj2ProcessBuilder onlyForTestGetProcessBuilder() {
		return builder;
	}

	public Object onlyForTestGetPollSleepTimeInMilliseconds() {
		return pollSleepTime;
	}

	public Object onlyForTestGetTimeoutInSeconds() {
		return timeoutInSeconds;
	}
}
