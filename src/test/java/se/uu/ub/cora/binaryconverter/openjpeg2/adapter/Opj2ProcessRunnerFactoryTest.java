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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessBuilder;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessBuilderImp;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessRunner;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessRunnerFactoryImp;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessRunnerImp;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ParametersSpy;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ProcessBuilderSpy;

public class Opj2ProcessRunnerFactoryTest {

	private Opj2ProcessRunnerFactoryImp factory;
	private Opj2ParametersSpy parameters;
	private Opj2ProcessRunnerImp processRunner;
	private Opj2ProcessBuilderSpy processBuilderSpy;

	@BeforeMethod
	private void beforeMethod() {
		parameters = new Opj2ParametersSpy();
		processBuilderSpy = new Opj2ProcessBuilderSpy();

		factory = new Opj2ProcessRunnerFactoryImp();

	}

	@Test
	public void testFactor() throws Exception {

		processRunner = (Opj2ProcessRunnerImp) factory.factor(parameters);

		assertTrue(processRunner instanceof Opj2ProcessRunner);

		assertProcessRunnerAndBuilder();

	}

	private void assertProcessRunnerAndBuilder() {
		Opj2ProcessBuilderImp processbuilder = (Opj2ProcessBuilderImp) processRunner
				.onlyForTestGetProcessBuilder();
		assertBuilder(processbuilder);

		assertEquals(processRunner.onlyForTestGetPollSleepTimeInMilliseconds(), 5000);
		assertEquals(processRunner.onlyForTestGetTimeoutInSeconds(), 900);
	}

	private void assertBuilder(Opj2ProcessBuilderImp processbuilder) {
		assertTrue(processbuilder instanceof Opj2ProcessBuilder);
		assertEquals(processbuilder.onlyForTestGetParameters(), parameters);

	}

	@Test
	public void testProcessBuilderCallsInheritIO() throws Exception {
		Opj2ProcessRunnerFactoryOnlyForTest factoryTest = new Opj2ProcessRunnerFactoryOnlyForTest();
		processRunner = (Opj2ProcessRunnerImp) factoryTest.factor(parameters);

		processBuilderSpy.MCR.assertMethodWasCalled("inheritIO");

	}

	class Opj2ProcessRunnerFactoryOnlyForTest extends Opj2ProcessRunnerFactoryImp {

		@Override
		Opj2ProcessBuilder createNewProcessBuilder(Opj2Parameters parameters) {
			return processBuilderSpy;
		}
	}
}
