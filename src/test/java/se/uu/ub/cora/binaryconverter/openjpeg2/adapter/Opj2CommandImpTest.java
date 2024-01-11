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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Command;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2CommandImp;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ParametersSpy;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ProcessRunnerFactorySpy;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ProcessRunnerSpy;

public class Opj2CommandImpTest {

	private Opj2Command command;

	private Opj2ProcessRunnerFactorySpy runnerFactory;
	private Opj2ParametersSpy parameters;

	@BeforeMethod
	private void beforeMethod() {
		runnerFactory = new Opj2ProcessRunnerFactorySpy();
		parameters = new Opj2ParametersSpy();

		command = new Opj2CommandImp(runnerFactory);
	}

	@Test
	public void testCompress() throws Exception {

		command.compress(parameters);

		parameters.MCR.assertParameters("opj2Command", 0, "opj2_compress");

		runnerFactory.MCR.assertParameters("factor", 0, parameters);

		Opj2ProcessRunnerSpy processRunner = (Opj2ProcessRunnerSpy) runnerFactory.MCR
				.getReturnValue("factor", 0);
		processRunner.MCR.assertParameters("runOpj2Process", 0);

	}

	@Test
	public void testDecompress() throws Exception {

		command.decompress(parameters);

		parameters.MCR.assertParameters("opj2Command", 0, "opj2_decompress");

		runnerFactory.MCR.assertParameters("factor", 0, parameters);

		Opj2ProcessRunnerSpy processRunner = (Opj2ProcessRunnerSpy) runnerFactory.MCR
				.getReturnValue("factor", 0);
		processRunner.MCR.assertParameters("runOpj2Process", 0);

	}
}
