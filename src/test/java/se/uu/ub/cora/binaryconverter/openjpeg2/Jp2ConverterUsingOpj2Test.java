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
import static org.testng.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ParametersSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterSpy;

public class Jp2ConverterUsingOpj2Test {

	private Jp2Converter converter;

	private Opj2CommandSpy opj2Command;
	private Opj2ParametersSpy opj2Parameters;
	private ImageConverterSpy converterToTiff;

	private static final String SOME_TEMP_INPUT_PATH = "./someTempInputPath";
	private static final String SOME_TEMP_OUTPUT_PATH = "./someTempOutputPath";

	@BeforeMethod
	private void beforeMethod() {
		opj2Command = new Opj2CommandSpy();
		opj2Parameters = new Opj2ParametersSpy();
		converterToTiff = new ImageConverterSpy();

		opj2Parameters.MRV.setDefaultReturnValuesSupplier("getOutputPath",
				() -> SOME_TEMP_OUTPUT_PATH + ".jp2");

		createFile(SOME_TEMP_INPUT_PATH);
		converter = new Jp2ConverterUsingOpj2(opj2Command, opj2Parameters, converterToTiff);
	}

	private void createFile(String pathToFile) {
		Path path = Paths.get(pathToFile);
		try {
			Files.createFile(path);
		} catch (IOException e) {
			fail("It could not create file: " + pathToFile);
		}
	}

	@AfterMethod
	private void afterMethod() {
		deleteFileIfExists(SOME_TEMP_INPUT_PATH);
		deleteFileIfExists(SOME_TEMP_OUTPUT_PATH);
	}

	private void deleteFileIfExists(String fileToDelete) {
		Path path = Paths.get(fileToDelete);
		try {
			if (Files.exists(path)) {
				Files.delete(path);
			}
		} catch (Exception e) {
			fail("It failed cleaning up the tests. " + e.getMessage());
		}
	}

	@Test
	public void testConvertToJp2UsingKnownMimeTypes() throws Exception {
		// Extensions known by openJPEG: BMP, PGM, PGX, PNG, PNM, PPM, RAW, RAWL, TGA, TIF
		assertConvertToJp2UsingKnownMimeType(0, ".bmp", "image/bmp");
		assertConvertToJp2UsingKnownMimeType(1, ".pgm", "image/x-portable-graymap");
		assertConvertToJp2UsingKnownMimeType(2, ".png", "image/png");
		assertConvertToJp2UsingKnownMimeType(3, ".pnm", "image/x-portable-anymap");
		assertConvertToJp2UsingKnownMimeType(4, ".ppm", "image/x-portable-pixmap");
		assertConvertToJp2UsingKnownMimeType(5, ".raw", "image/x-raw-panasonic");
		assertConvertToJp2UsingKnownMimeType(6, ".tga", "image/x-tga");
		assertConvertToJp2UsingKnownMimeType(7, ".tif", "image/tiff");
		// assertConvertToJp2UsingKnownMimeType(8, ".pgx", "Not found by tika");
		// assertConvertToJp2UsingKnownMimeType(9, ".rawl", "Not found by tika");

		opj2Command.MCR.assertNumberOfCallsToMethod("compress", 8);

		converterToTiff.MCR.assertMethodNotCalled("convertToTiff");

	}

	private void assertConvertToJp2UsingKnownMimeType(int callNr, String extension,
			String mimeType) {
		opj2Parameters.MRV.setDefaultReturnValuesSupplier("getInputPath",
				() -> SOME_TEMP_INPUT_PATH + extension);

		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, mimeType);

		assertOpj2Parameters(callNr, SOME_TEMP_INPUT_PATH + extension);

		opj2Command.MCR.assertParameters("compress", callNr, opj2Parameters);

		Path pathWithExtension = Paths.get(SOME_TEMP_INPUT_PATH + extension);
		assertFalse(Files.exists(pathWithExtension));
	}

	private void assertOpj2Parameters(int callNr, String inputPath) {
		opj2Parameters.MCR.assertParameters("inputPath", callNr, inputPath);
		opj2Parameters.MCR.assertParameters("outputPath", callNr, SOME_TEMP_OUTPUT_PATH + ".jp2");
		opj2Parameters.MCR.assertParameters("codeBlockSize", callNr, 64, 64);
		assertParameterArrayValues("precinctSize", "precinctSize", List.of(256, 256));
		opj2Parameters.MCR.assertParameters("tileSize", callNr, 1024, 1024);
		opj2Parameters.MCR.assertParameters("numOfResolutions", callNr, 7);
		assertParameterArrayValues("psnrQuality", "psnrLayers", List.of(25, 28, 30, 35, 40));
		opj2Parameters.MCR.assertParameters("progressionOrder", callNr, "RPCL");
		opj2Parameters.MCR.assertParameters("enableEph", callNr);
		opj2Parameters.MCR.assertParameters("enableSop", callNr);
		opj2Parameters.MCR.assertParameters("enableTlm", callNr);
		opj2Parameters.MCR.assertParameters("enablePlt", callNr);
		opj2Parameters.MCR.assertParameters("tilePartDivider", callNr, "R");
		opj2Parameters.MCR.assertParameters("numberOfThreads", callNr, 6);
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
	public void testOuptutFileJp2MovedToFileWithoutExtension() throws Exception {
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/bmp");

		Path pathWithJp2 = Paths.get(SOME_TEMP_OUTPUT_PATH + ".jp2");
		assertFalse(Files.exists(pathWithJp2));

		Path path = Paths.get(SOME_TEMP_OUTPUT_PATH);
		assertTrue(Files.exists(path));
	}

	@Test
	public void testMimeTypeNotAcceptedConvertToTiff() throws Exception {

		long before = System.currentTimeMillis();
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/nonAcceptedMimeType");
		long after = System.currentTimeMillis();

		converterToTiff.MCR.assertParameters("convertToTiff", 0, SOME_TEMP_INPUT_PATH);
		String tempFilePath = assertTempFilePathCreation(before, after);
		assertOpj2Parameters(0, tempFilePath);
		opj2Command.MCR.assertParameters("compress", 0, opj2Parameters);

		Path path = Paths.get(tempFilePath);
		assertFalse(Files.exists(path));
	}

	private String assertTempFilePathCreation(long before, long after) {
		String tempFilePath = (String) converterToTiff.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("convertToTiff", 0,
						"outputPath");

		assertTrue(tempFilePath.startsWith("/tmp/"));
		assertTrue(tempFilePath.endsWith(".tif"));
		String outputFileName = getTimestampFromFileName(tempFilePath);

		Long outputFileNameAsLong = Long.valueOf(outputFileName);
		assertTrue(before <= outputFileNameAsLong);
		assertTrue(after >= outputFileNameAsLong);
		return tempFilePath;
	}

	private String getTimestampFromFileName(String tempFilePath) {
		return tempFilePath.substring("/tmp/".length(), tempFilePath.length() - (".tif".length()));
	}

	@Test
	public void testExceptionOnCreateSymbolicLink() throws Exception {

		createFile("/tmp/nonExistingInput.png");
		try {
			converter.convert("/tmp/nonExistingInput", SOME_TEMP_OUTPUT_PATH, "image/png");
			fail("it should throw exception");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertEquals(e.getMessage(),
					"Error converting to OpenJpg2, could not create symbolic link for file /tmp/nonExistingInput");
			assertEquals(e.getCause().toString(),
					"java.nio.file.FileAlreadyExistsException: /tmp/nonExistingInput.png");
		} finally {
			deleteFileIfExists("/tmp/nonExistingInput.png");
		}
	}

}
