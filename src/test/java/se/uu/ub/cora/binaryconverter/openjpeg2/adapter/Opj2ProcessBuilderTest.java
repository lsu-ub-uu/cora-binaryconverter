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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.OpenJpeg2Exception;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessBuilder;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessBuilderImp;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ParametersSpy;

public class Opj2ProcessBuilderTest {

	private Opj2ProcessBuilderImp opj2ProcessBuilder;
	private Opj2ParametersSpy parameters;

	@BeforeMethod
	private void beforeMethod() {
		parameters = new Opj2ParametersSpy();

		parameters.MRV.setDefaultReturnValuesSupplier("getParamsList", () -> List.of("pwd"));

		opj2ProcessBuilder = new Opj2ProcessBuilderImp(parameters);
	}

	@Test
	public void testInit() throws Exception {
		assertTrue(opj2ProcessBuilder instanceof Opj2ProcessBuilder);

		ProcessBuilder processBuilder = opj2ProcessBuilder.onlyForTestGetProcessBuilder();
		assertEquals(processBuilder.command().size(), 1);
		assertEquals(processBuilder.command().get(0), "pwd");
	}

	@Test
	public void testStart() throws Exception {
		Process process = opj2ProcessBuilder.start();

		assertNotNull(process);

	}

	@Test
	public void testStartThrowsException() throws Exception {
		opj2ProcessBuilder = new Opj2ProcessBuilderOnlyForTest(parameters);

		try {
			opj2ProcessBuilder.start();
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof OpenJpeg2Exception);
			assertEquals(e.getMessage(), "spyException");
		}

	}

	class Opj2ProcessBuilderOnlyForTest extends Opj2ProcessBuilderImp {
		public Opj2ProcessBuilderOnlyForTest(Opj2Parameters parameters) {
			super(parameters);
		}

		@Override
		Process runStart() throws IOException {
			throw new IOException("spyException");
		}
	}

	@Test
	public void testInheritIO() throws Exception {

		Opj2ProcessBuilderOnlyForTest2 opj2ProcessBuilder = new Opj2ProcessBuilderOnlyForTest2(
				parameters);

		Opj2ProcessBuilderImp inheritIO = (Opj2ProcessBuilderImp) opj2ProcessBuilder.inheritIO();
		opj2ProcessBuilder.start();

		assertSame(inheritIO.onlyForTestGetProcessBuilder(),
				opj2ProcessBuilder.onlyForTestGetProcessBuilder());

		Redirect redirectOutput = opj2ProcessBuilder.redirectOutput();
		assertEquals(redirectOutput.toString(), "INHERIT");
	}

	class Opj2ProcessBuilderOnlyForTest2 extends Opj2ProcessBuilderImp {

		public Opj2ProcessBuilderOnlyForTest2(Opj2Parameters parameters) {
			super(parameters);
		}

		Redirect redirectOutput() {
			return processBuilder.redirectOutput();
		}
	}
}
