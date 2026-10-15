/*
 * Copyright 2023, 2026 Uppsala University Library
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
	private final OpjProcessBuilder builder;
	private long timeoutInSeconds;

	public OpjProcessRunnerImp(OpjProcessBuilder builder, long timeoutInSeconds) {
		this.builder = builder;
		this.timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public void runOpjProcess() {
		Process process = builder.start();
		tryToWaitForConvertingToFinish(process);
	}

	private void tryToWaitForConvertingToFinish(Process process) {
		try {
			waitForConvertingToFinish(process);
		} catch (InterruptedException e) {
			process.destroyForcibly();
			// Not needed here: Thread.currentThread().interrupt();
			throw OpenJpegException.withMessageAndException(
					"Openjpeg2 conversion was interupted while waiting for conversion to finish.",
					e);
		}

	}

	private void waitForConvertingToFinish(Process process) throws InterruptedException {
		final boolean finished;
		finished = process.waitFor(timeoutInSeconds, java.util.concurrent.TimeUnit.SECONDS);
		throwErrorIfNotFinished(process, finished);

		int exitCode = process.exitValue();
		throwErrorIfFailed(exitCode);
	}

	private void throwErrorIfNotFinished(Process process, final boolean finished) {
		if (!finished) {
			destroyProcess(process);
			throw OpenJpegException.withMessage(
					"Openjpeg2 conversion timed out after " + timeoutInSeconds + " seconds.");
		}
	}

	private void throwErrorIfFailed(int exitCode) {
		if (exitCode != 0) {
			throw OpenJpegException
					.withMessage("Openjpeg2 conversion failed with exit code " + exitCode + ".");
		}
	}

	private void destroyProcess(Process process) {
		try {
			process.destroy();
			process.waitFor(15L, java.util.concurrent.TimeUnit.SECONDS);
			if (process.isAlive()) {
				process.destroyForcibly();
			}
		} catch (InterruptedException e) {
			process.destroyForcibly();
			// Not needed here: Thread.currentThread().interrupt();
		}
	}

	public OpjProcessBuilder onlyForTestGetProcessBuilder() {
		return builder;
	}

	public Object onlyForTestGetTimeoutInSeconds() {
		return timeoutInSeconds;
	}
}
