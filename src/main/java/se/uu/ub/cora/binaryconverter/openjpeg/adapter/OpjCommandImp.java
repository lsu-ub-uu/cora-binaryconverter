/*
 * Copyright 2023, 2024 Uppsala University Library
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

public class OpjCommandImp implements OpjCommand {

	private OpjProcessRunnerFactory processRunnerFactory;

	public OpjCommandImp(OpjProcessRunnerFactory processRunnerFactory) {
		this.processRunnerFactory = processRunnerFactory;
	}

	@Override
	public void compress(OpjParameters parameters) {
		runProcess(parameters, "opj_compress");
	}

	private void runProcess(OpjParameters parameters, String command) {
		parameters.opjCommand(command);
		OpjProcessRunner runner = processRunnerFactory.factor(parameters);
		runner.runOpjProcess();
	}

	@Override
	public void decompress(OpjParameters parameters) {
		runProcess(parameters, "opj_decompress");
	}

	public OpjProcessRunnerFactory onlyForTestGetOpjProcessRunnerFactory() {
		return processRunnerFactory;
	}
}