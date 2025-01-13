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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjParametersSpy;
import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjProcessBuilderSpy;

public class OpjProcessRunnerFactoryTest {

	private OpjProcessRunnerFactoryImp factory;
	private OpjParametersSpy parameters;
	private OpjProcessRunnerImp processRunner;
	private OpjProcessBuilderSpy processBuilderSpy;

	@BeforeMethod
	private void beforeMethod() {
		parameters = new OpjParametersSpy();
		processBuilderSpy = new OpjProcessBuilderSpy();

		factory = new OpjProcessRunnerFactoryImp();

	}

	@Test
	public void testFactor() throws Exception {

		processRunner = (OpjProcessRunnerImp) factory.factor(parameters);

		assertTrue(processRunner instanceof OpjProcessRunner);

		assertProcessRunnerAndBuilder();

	}

	private void assertProcessRunnerAndBuilder() {
		OpjProcessBuilderImp processbuilder = (OpjProcessBuilderImp) processRunner
				.onlyForTestGetProcessBuilder();
		assertBuilder(processbuilder);

		assertEquals(processRunner.onlyForTestGetPollSleepTimeInMilliseconds(), 5000);
		assertEquals(processRunner.onlyForTestGetTimeoutInSeconds(), 900);
	}

	private void assertBuilder(OpjProcessBuilderImp processbuilder) {
		assertTrue(processbuilder instanceof OpjProcessBuilder);
		assertEquals(processbuilder.onlyForTestGetParameters(), parameters);

	}

	@Test
	public void testProcessBuilderCallsInheritIO() throws Exception {
		OpjProcessRunnerFactoryOnlyForTest factoryTest = new OpjProcessRunnerFactoryOnlyForTest();
		processRunner = (OpjProcessRunnerImp) factoryTest.factor(parameters);

		processBuilderSpy.MCR.assertMethodWasCalled("inheritIO");

	}

	class OpjProcessRunnerFactoryOnlyForTest extends OpjProcessRunnerFactoryImp {

		@Override
		OpjProcessBuilder createNewProcessBuilder(OpjParameters parameters) {
			return processBuilderSpy;
		}
	}
}
