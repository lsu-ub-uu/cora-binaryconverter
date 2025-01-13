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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjParametersSpy;
import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjProcessRunnerFactorySpy;
import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjProcessRunnerSpy;

public class OpjCommandImpTest {

	private OpjCommand command;

	private OpjProcessRunnerFactorySpy runnerFactory;
	private OpjParametersSpy parameters;

	@BeforeMethod
	private void beforeMethod() {
		runnerFactory = new OpjProcessRunnerFactorySpy();
		parameters = new OpjParametersSpy();

		command = new OpjCommandImp(runnerFactory);
	}

	@Test
	public void testCompress() throws Exception {

		command.compress(parameters);

		parameters.MCR.assertParameters("opjCommand", 0, "opj_compress");

		runnerFactory.MCR.assertParameters("factor", 0, parameters);

		OpjProcessRunnerSpy processRunner = getProcesseRunner();
		processRunner.MCR.assertParameters("runOpjProcess", 0);

	}

	@Test
	public void testDecompress() throws Exception {

		command.decompress(parameters);

		parameters.MCR.assertParameters("opjCommand", 0, "opj_decompress");

		runnerFactory.MCR.assertParameters("factor", 0, parameters);

		OpjProcessRunnerSpy processRunner = getProcesseRunner();
		processRunner.MCR.assertParameters("runOpjProcess", 0);

	}

	private OpjProcessRunnerSpy getProcesseRunner() {
		OpjProcessRunnerSpy processRunner = (OpjProcessRunnerSpy) runnerFactory.MCR
				.getReturnValue("factor", 0);
		return processRunner;
	}

	@Test
	public void testOnlyForTest() throws Exception {

		OpjCommandImp commandImp = (OpjCommandImp) command;
		assertEquals(commandImp.onlyForTestGetOpjProcessRunnerFactory(), runnerFactory);

	}
}
