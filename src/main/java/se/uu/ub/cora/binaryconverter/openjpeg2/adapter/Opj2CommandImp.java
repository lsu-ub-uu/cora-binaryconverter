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

public class Opj2CommandImp implements Opj2Command {

	private Opj2ProcessRunnerFactory processRunnerFactory;

	public Opj2CommandImp(Opj2ProcessRunnerFactory processRunnerFactory) {
		this.processRunnerFactory = processRunnerFactory;
	}

	@Override
	public void compress(Opj2Parameters parameters) {
		runProcess(parameters, "opj2_compress");
	}

	private void runProcess(Opj2Parameters parameters, String command) {
		parameters.opj2Command(command);
		Opj2ProcessRunner runner = processRunnerFactory.factor(parameters);
		runner.runOpj2Process();
	}

	@Override
	public void decompress(Opj2Parameters parameters) {
		runProcess(parameters, "opj2_decompress");
	}

	public Opj2ProcessRunnerFactory onlyForTestGetOpj2ProcessRunnerFactory() {
		return processRunnerFactory;
	}
}