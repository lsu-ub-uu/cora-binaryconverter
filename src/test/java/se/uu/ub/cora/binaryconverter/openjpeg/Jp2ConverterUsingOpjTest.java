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
package se.uu.ub.cora.binaryconverter.openjpeg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjParametersSpy;
import se.uu.ub.cora.binaryconverter.spy.FilesWrapperSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterSpy;

public class Jp2ConverterUsingOpjTest {

	private Jp2Converter converter;

	private OpjCommandSpy opjCommand;
	private OpjParametersSpy opjParameters;
	private ImageConverterSpy converterToTiff;
	private FilesWrapperSpy filesWrapper;

	private static final String SOME_TEMP_INPUT_PATH = "./someTempInputPath";
	private static final String SOME_TEMP_OUTPUT_PATH = "./someTempOutputPath";

	@BeforeMethod
	private void beforeMethod() {
		opjCommand = new OpjCommandSpy();
		opjParameters = new OpjParametersSpy();
		converterToTiff = new ImageConverterSpy();
		filesWrapper = new FilesWrapperSpy();

		opjParameters.MRV.setDefaultReturnValuesSupplier("getOutputPath",
				() -> SOME_TEMP_OUTPUT_PATH + ".jp2");

		converter = new Jp2ConverterUsingOpj(opjCommand, opjParameters, converterToTiff,
				filesWrapper);
	}

	@Test
	public void testConvertToJp2UsingKnownMimeTypes() throws Exception {
		// Extensions known by openJPEG: BMP, PGM, PGX, PNG, PNM, PPM, RAW, RAWL, TGA, TIF
		assertConvertToJp2UsingKnownMimeType(0, ".bmp", "image/bmp");
		assertConvertToJp2UsingKnownMimeType(1, ".pgm", "image/x-portable-graymap");
		assertConvertToJp2UsingKnownMimeType(2, ".png", "image/png");
		assertConvertToJp2UsingKnownMimeType(3, ".pnm", "image/x-portable-anymap");
		assertConvertToJp2UsingKnownMimeType(4, ".ppm", "image/x-portable-pixmap");
		assertConvertToJp2UsingKnownMimeType(5, ".tga", "image/x-tga");
		assertConvertToJp2UsingKnownMimeType(6, ".tif", "image/tiff");
		// assertConvertToJp2UsingKnownMimeType(8, ".pgx", "Not found by tika");
		// assertConvertToJp2UsingKnownMimeType(9, ".rawl", "Not found by tika");

		opjCommand.MCR.assertNumberOfCallsToMethod("compress", 7);

		converterToTiff.MCR.assertMethodNotCalled("convertToTiff");
	}

	private void assertConvertToJp2UsingKnownMimeType(int callNr, String extension,
			String mimeType) {
		opjParameters.MRV.setDefaultReturnValuesSupplier("getInputPath",
				() -> SOME_TEMP_INPUT_PATH + extension);

		long before = System.currentTimeMillis();
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, mimeType);
		long after = System.currentTimeMillis();

		String tempSymbolicLink = assertCallToCreateSymbolicLink(callNr, extension, before, after);

		assertOpjParameters(callNr, tempSymbolicLink);
		opjCommand.MCR.assertParameters("compress", callNr, opjParameters);

		filesWrapper.MCR.assertParameters("move", callNr, SOME_TEMP_OUTPUT_PATH + ".jp2",
				SOME_TEMP_OUTPUT_PATH);
		filesWrapper.MCR.assertParameters("delete", callNr, tempSymbolicLink);
	}

	private String assertCallToCreateSymbolicLink(int callNr, String extension, long before,
			long after) {
		String tempSymbolicLink = (String) filesWrapper.MCR
				.getParameterForMethodAndCallNumberAndParameter("createSymbolicLink", callNr,
						"link");

		assertCreationOfTempFile(tempSymbolicLink, extension, before, after);
		filesWrapper.MCR.assertParameter("createSymbolicLink", callNr, "target",
				SOME_TEMP_INPUT_PATH);
		return tempSymbolicLink;
	}

	private void assertOpjParameters(int callNr, String inputPath) {
		opjParameters.MCR.assertParameters("inputPath", callNr, inputPath);
		opjParameters.MCR.assertParameters("outputPath", callNr, SOME_TEMP_OUTPUT_PATH + ".jp2");
		opjParameters.MCR.assertParameters("codeBlockSize", callNr, 64, 64);
		assertParameterArrayValues("precinctSize", "precinctSize", List.of(256, 256));
		opjParameters.MCR.assertParameters("tileSize", callNr, 1024, 1024);
		opjParameters.MCR.assertParameters("numOfResolutions", callNr, 7);
		assertParameterArrayValues("psnrQuality", "psnrLayers", List.of(60));
		opjParameters.MCR.assertParameters("progressionOrder", callNr, "RPCL");
		opjParameters.MCR.assertParameters("enableEph", callNr);
		opjParameters.MCR.assertParameters("enableSop", callNr);
		opjParameters.MCR.assertParameters("enableTlm", callNr);
		opjParameters.MCR.assertParameters("enablePlt", callNr);
		opjParameters.MCR.assertParameters("tilePartDivider", callNr, "R");
		opjParameters.MCR.assertParameters("numberOfThreads", callNr, 6);
	}

	private void assertParameterArrayValues(String method, String parameter,
			List<Integer> expectedValues) {
		int[] array = (int[]) opjParameters.MCR
				.getParameterForMethodAndCallNumberAndParameter(method, 0, parameter);

		assertEquals(array.length, expectedValues.size());

		int element = 0;
		for (Integer value : expectedValues) {
			assertEquals(array[element], value);
			element++;
		}
	}

	@Test
	public void testMimeTypeNotAcceptedConvertToTiff() throws Exception {
		long before = System.currentTimeMillis();
		converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/nonAcceptedMimeType");
		long after = System.currentTimeMillis();

		converterToTiff.MCR.assertParameters("convertToTiff", 0, SOME_TEMP_INPUT_PATH);
		String tempTifFile = (String) converterToTiff.MCR
				.getParameterForMethodAndCallNumberAndParameter("convertToTiff", 0, "outputPath");
		assertCreationOfTempFile(tempTifFile, ".tif", before, after);

		assertOpjParameters(0, tempTifFile);
		opjCommand.MCR.assertParameters("compress", 0, opjParameters);
		filesWrapper.MCR.assertParameters("delete", 0, tempTifFile);
		filesWrapper.MCR.assertParameters("move", 0, SOME_TEMP_OUTPUT_PATH + ".jp2",
				SOME_TEMP_OUTPUT_PATH);
	}

	private void assertCreationOfTempFile(String tempFile, String extension, long before,
			long after) {
		assertTrue(tempFile.startsWith("/tmp/"));
		assertTrue(tempFile.endsWith(extension));

		Long outputFileNameAsLong = getTimestampFromFileName(tempFile);
		assertTrue(before <= outputFileNameAsLong);
		assertTrue(after >= outputFileNameAsLong);
	}

	private Long getTimestampFromFileName(String tempFilePath) {
		String timestamp = tempFilePath.substring("/tmp/".length(),
				tempFilePath.length() - (".tif".length()));
		return Long.valueOf(timestamp);
	}

	@Test
	public void testExceptionOnCreateSymbolicLink() throws Exception {
		filesWrapper.MRV.setAlwaysThrowException("delete",
				BinaryConverterException.withMessage("someSpyException"));
		try {
			converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH, "image/png");
			fail("it should throw exception");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertEquals(e.getMessage(), "Error converting to jp2: someSpyException");
			assertEquals(e.getCause().toString(),
					"se.uu.ub.cora.binaryconverter.internal.BinaryConverterException: someSpyException");
		}
	}

}
