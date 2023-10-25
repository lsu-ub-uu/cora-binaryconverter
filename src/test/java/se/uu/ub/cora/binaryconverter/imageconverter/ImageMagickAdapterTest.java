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
package se.uu.ub.cora.binaryconverter.imageconverter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.spy.ArrayListOutputConsumerSpy;
import se.uu.ub.cora.binaryconverter.imageconverter.spy.IMOperationSpy;
import se.uu.ub.cora.binaryconverter.imageconverter.spy.IdentifyCmdSpy;

public class ImageMagickAdapterTest {

	private static final String FORMAT_DPI_WIDTH_HEIGHT = "%xx%y %w %h";
	private static final String SOME_TEMP_PATH = "/someTempPath";
	ImageMagickAdapaterImp imageMagick;

	private IdentifyCmdSpy identifyCmd;
	private IMOperationSpy imOperation;
	private ArrayListOutputConsumerSpy outputConsumer;

	@BeforeMethod
	public void beforeMethod() {
		imageMagick = new ImageMagickAdapaterImp();

		identifyCmd = new IdentifyCmdSpy();
		imOperation = new IMOperationSpy();
		outputConsumer = new ArrayListOutputConsumerSpy();

		ArrayList<String> returnedOutput = new ArrayList<>(List.of("72x72 2560 1440"));
		outputConsumer.MRV.setDefaultReturnValuesSupplier("getOutput", () -> returnedOutput);

		imageMagick.onlyForTestSetIdentifyCmd(identifyCmd);
		imageMagick.onlyForTestSetIMOperation(imOperation);
		imageMagick.onlyForTestSetArrayListOutputConsumer(outputConsumer);

	}

	@Test
	public void testAnalyzeImage() throws Exception {

		ImageData imageData = imageMagick.analyze(SOME_TEMP_PATH);

		assertEquals(imageData.resolution(), "72x72");
		assertEquals(imageData.width(), "2560");
		assertEquals(imageData.height(), "1440");
	}

	@Test
	public void tesAnalyzeImageCallsImageMagick() throws Exception {

		imageMagick.analyze(SOME_TEMP_PATH);

		String[] pathAsArray = (String[]) imOperation.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("addImage", 0, "arg0");

		assertEquals(pathAsArray[0], SOME_TEMP_PATH);
		imOperation.MCR.assertParameters("format", 0, FORMAT_DPI_WIDTH_HEIGHT);
		var formatIMOps = imOperation.MCR.getReturnValue("format", 0);

		identifyCmd.MCR.assertParameters("setOutputConsumer", 0, outputConsumer);
		identifyCmd.MCR.assertParameters("run", 0, formatIMOps);
		outputConsumer.MCR.methodWasCalled("getOutput");

	}

	@Test
	public void testAnalyzeThrowImageConverterException() throws Exception {
		identifyCmd.MRV.setAlwaysThrowException("run", new RuntimeException("Error from spy"));
		try {
			imageMagick.analyze(SOME_TEMP_PATH);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof ImageConverterException);
			assertEquals(e.getMessage(),
					"Error when analyzing image, with path: " + SOME_TEMP_PATH);
			assertEquals(e.getCause().getMessage(), "Error from spy");
		}
	}

	@Test
	public void testAnalyzeImageMagickReturnLessValuesThanExpected() throws Exception {
		ArrayList<String> returnedOutput = new ArrayList<>(List.of("2560 1440"));
		outputConsumer.MRV.setDefaultReturnValuesSupplier("getOutput", () -> returnedOutput);

		try {
			imageMagick.analyze(SOME_TEMP_PATH);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof ImageConverterException);
			assertEquals(e.getMessage(),
					"Error when analyzing image, with path: " + SOME_TEMP_PATH);
			assertEquals(e.getCause().getMessage(), "Index 2 out of bounds for length 2");
		}
	}

	@Test(enabled = false)
	public void testRealAnalyze() throws Exception {
		ImageMagickAdapaterImp imageMagickReal = new ImageMagickAdapaterImp();

		ImageData analyze = imageMagickReal.analyze("/home/pere/workspace/gokuForever.jpg");

		System.out.println("ImageData" + analyze);
	}

}
