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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjParametersSpy;

public class OpjProcessBuilderTest {

	private OpjProcessBuilderImp opjProcessBuilder;
	private OpjParametersSpy parameters;

	@BeforeMethod
	private void beforeMethod() {
		parameters = new OpjParametersSpy();

		parameters.MRV.setDefaultReturnValuesSupplier("getParamsList", () -> List.of("pwd"));

		opjProcessBuilder = new OpjProcessBuilderImp(parameters);
	}

	@Test
	public void testInit() throws Exception {
		assertTrue(opjProcessBuilder instanceof OpjProcessBuilder);

		ProcessBuilder processBuilder = opjProcessBuilder.onlyForTestGetProcessBuilder();
		assertEquals(processBuilder.command().size(), 1);
		assertEquals(processBuilder.command().get(0), "pwd");
	}

	@Test
	public void testStart() throws Exception {
		Process process = opjProcessBuilder.start();

		assertNotNull(process);

	}

	@Test
	public void testStartThrowsException() throws Exception {
		opjProcessBuilder = new OpjProcessBuilderOnlyForTest(parameters);

		try {
			opjProcessBuilder.start();
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof OpenJpegException);
			assertEquals(e.getMessage(), "Cannot start process builder for Opj");
			assertEquals(e.getCause().getMessage(), "spyException");
		}

	}

	class OpjProcessBuilderOnlyForTest extends OpjProcessBuilderImp {
		public OpjProcessBuilderOnlyForTest(OpjParameters parameters) {
			super(parameters);
		}

		@Override
		Process runStart() throws IOException {
			throw new IOException("spyException");
		}
	}

	@Test
	public void testInheritIO() throws Exception {

		OpjProcessBuilderOnlyForTest2 opjProcessBuilder = new OpjProcessBuilderOnlyForTest2(
				parameters);

		OpjProcessBuilderImp inheritIO = (OpjProcessBuilderImp) opjProcessBuilder.inheritIO();
		opjProcessBuilder.start();

		assertSame(inheritIO.onlyForTestGetProcessBuilder(),
				opjProcessBuilder.onlyForTestGetProcessBuilder());

		Redirect redirectOutput = opjProcessBuilder.redirectOutput();
		assertEquals(redirectOutput.toString(), "INHERIT");
	}

	class OpjProcessBuilderOnlyForTest2 extends OpjProcessBuilderImp {

		public OpjProcessBuilderOnlyForTest2(OpjParameters parameters) {
			super(parameters);
		}

		Redirect redirectOutput() {
			return processBuilder.redirectOutput();
		}
	}
}
