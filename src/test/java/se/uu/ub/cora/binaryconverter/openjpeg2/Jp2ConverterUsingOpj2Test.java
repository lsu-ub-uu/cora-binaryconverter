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
package se.uu.ub.cora.binaryconverter.openjpeg2;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ParametersSpy;

public class Jp2ConverterUsingOpj2Test {

	private Jp2Converter converter;

	private Opj2CommandSpy opj2Command;
	private Opj2ParametersSpy opj2Parameters;

	private static final String SOME_TEMP_INPUT_PATH = "/someTempInputPath";
	private static final String SOME_TEMP_OUTPUT_PATH = "/someTempOutputPath";

	@BeforeMethod
	private void beforeMethod() {
		opj2Command = new Opj2CommandSpy();
		opj2Parameters = new Opj2ParametersSpy();

		converter = new Jp2ConverterUsingOpj2(opj2Command, opj2Parameters);

	}

	@Test
	public void testConvertFillOpj2Command() throws Exception {

		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH);

		opj2Parameters.MCR.assertParameters("inputPath", 0, SOME_TEMP_INPUT_PATH);
		opj2Parameters.MCR.assertParameters("outputPath", 0, SOME_TEMP_OUTPUT_PATH);
		opj2Parameters.MCR.assertParameters("codeBlockSize", 0, 64, 64);
		assertParameterArrayValues("precinctSize", "precinctSize", List.of(256, 256));
		opj2Parameters.MCR.assertParameters("tileSize", 0, 1024, 1024);
		opj2Parameters.MCR.assertParameters("numOfResolutions", 0, 7);
		assertParameterArrayValues("psnrQuality", "psnrLayers", List.of(25, 28, 30, 35, 40));
		opj2Parameters.MCR.assertParameters("progressionOrder", 0, "RPCL");
		opj2Parameters.MCR.assertParameters("enableEph", 0);
		opj2Parameters.MCR.assertParameters("enableSop", 0);
		opj2Parameters.MCR.assertParameters("enableTlm", 0);
		opj2Parameters.MCR.assertParameters("enablePlt", 0);
		opj2Parameters.MCR.assertParameters("tilePartDivider", 0, "R");
		opj2Parameters.MCR.assertParameters("numberOfThreads", 0, 6);

		opj2Command.MCR.assertParameters("compress", 0, opj2Parameters);
	}

	private void assertParameterArrayValues(String method, String parameter,
			List<Integer> expectedValues) {
		int[] array = (int[]) opj2Parameters.MCR
				.getValueForMethodNameAndCallNumberAndParameterName(method, 0, parameter);

		assertEquals(array.length, expectedValues.size());

		int element = 0;
		for (Integer value : expectedValues) {
			assertEquals(array[element], value);
			element++;
		}
	}

}
