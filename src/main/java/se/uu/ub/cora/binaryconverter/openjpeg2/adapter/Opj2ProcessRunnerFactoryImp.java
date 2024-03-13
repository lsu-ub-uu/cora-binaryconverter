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

public class Opj2ProcessRunnerFactoryImp implements Opj2ProcessRunnerFactory {

	private static final int TIMEOUT_IN_SECONDS = 900;
	private static final int POLL_SLEEP_IN_MILLISECONDS = 5000;

	@Override
	public Opj2ProcessRunner factor(Opj2Parameters parameters) {
		Opj2ProcessBuilder processBuilder = createNewProcessBuilder(parameters);
		processBuilder.inheritIO();
		return new Opj2ProcessRunnerImp(processBuilder, POLL_SLEEP_IN_MILLISECONDS,
				TIMEOUT_IN_SECONDS);
	}

	// Needed for test
	Opj2ProcessBuilder createNewProcessBuilder(Opj2Parameters parameters) {
		return new Opj2ProcessBuilderImp(parameters);
	}
}
