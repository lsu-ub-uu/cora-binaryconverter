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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ParametersSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterSpy;

public class Jp2ConverterUsingOpj2Test {

	private Jp2Converter converter;

	private Opj2CommandSpy opj2Command;
	private Opj2ParametersSpy opj2Parameters;
	private ImageConverterSpy converterToTiff;

	private static final String SOME_TEMP_INPUT_PATH = "/someTempInputPath";
	private static final String SOME_TEMP_OUTPUT_PATH = "/someTempOutputPath";

	@BeforeMethod
	private void beforeMethod() {
		opj2Command = new Opj2CommandSpy();
		opj2Parameters = new Opj2ParametersSpy();
		converterToTiff = new ImageConverterSpy();

		converter = new Jp2ConverterUsingOpj2(opj2Command, opj2Parameters, converterToTiff);
	}

	@Test
	public void testConvertFillOpj2Command() throws Exception {

		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/bmp");

		assertOpj2Parameters(SOME_TEMP_INPUT_PATH);

		opj2Command.MCR.assertParameters("compress", 0, opj2Parameters);
	}

	private void assertOpj2Parameters(String inputPath) {
		opj2Parameters.MCR.assertParameters("inputPath", 0, inputPath);
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

	@Test
	public void testMimeTypesAcceptedForOpenJpeg2() throws Exception {

		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/bmp");
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/x-portable-graymap");
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/png");
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/x-portable-anymap");
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/x-portable-pixmap");
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/x-raw-panasonic");
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/x-tga");
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/tiff");

		opj2Command.MCR.assertNumberOfCallsToMethod("compress", 8);

		converterToTiff.MCR.assertMethodNotCalled("convertToTiff");
	}

	@Test
	public void testMimeTypeNotAcceptedConvertToTiff() throws Exception {

		long before = System.currentTimeMillis();
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/nonAcceptedMimeType");
		long after = System.currentTimeMillis();

		converterToTiff.MCR.assertParameters("convertToTiff", 0, SOME_TEMP_INPUT_PATH);
		String tempFilePath = assertTempFilePathCreation(before, after);
		assertOpj2Parameters(tempFilePath);
		opj2Command.MCR.assertParameters("compress", 0, opj2Parameters);

		Path path = Paths.get(tempFilePath);
		assertFalse(Files.exists(path));
	}

	private String assertTempFilePathCreation(long before, long after) {
		String tempFilePath = (String) converterToTiff.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("convertToTiff", 0,
						"outputPath");

		assertTrue(tempFilePath.startsWith("/tmp/"));
		String outputFileName = tempFilePath.substring("/tmp/".length(), tempFilePath.length());

		Long outputFileNameAsLong = Long.valueOf(outputFileName);
		assertTrue(before <= outputFileNameAsLong);
		assertTrue(after >= outputFileNameAsLong);
		return tempFilePath;
	}

}
