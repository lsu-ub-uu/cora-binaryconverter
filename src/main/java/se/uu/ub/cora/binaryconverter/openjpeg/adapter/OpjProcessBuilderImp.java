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

import java.io.IOException;

public class OpjProcessBuilderImp implements OpjProcessBuilder {

	ProcessBuilder processBuilder;
	private OpjParameters parameters;

	public OpjProcessBuilderImp(OpjParameters parameters) {
		this.parameters = parameters;
		processBuilder = new ProcessBuilder(parameters.getParamsList());
	}

	@Override
	public Process start() {
		try {
			return runStart();
		} catch (IOException e) {
			throw OpenJpegException.withMessageAndException("Cannot start process builder for Opj",
					e);
		}
	}

	Process runStart() throws IOException {
		return processBuilder.start();
	}

	ProcessBuilder onlyForTestGetProcessBuilder() {
		return processBuilder;
	}

	@Override
	public OpjProcessBuilder inheritIO() {
		processBuilder.inheritIO();
		return this;
	}

	public OpjParameters onlyForTestGetParameters() {
		return parameters;
	}

}
