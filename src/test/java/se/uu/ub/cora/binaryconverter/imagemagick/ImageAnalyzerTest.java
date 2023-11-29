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
package se.uu.ub.cora.binaryconverter.imagemagick;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageConverterException;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.imagemagick.ImageAnalyzerImp;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.ArrayListOutputConsumerSpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IMOperationSpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IdentifyCmdSpy;

public class ImageAnalyzerTest {

	private static final String FORMAT_DPI_WIDTH_HEIGHT_SIZE = "%xx%y %w %h %B";
	private static final String SOME_TEMP_PATH = "/someTempPath";
	ImageAnalyzerImp imageMagick;

	private IdentifyCmdSpy identifyCmd;
	private IMOperationSpy imOperation;
	private ArrayListOutputConsumerSpy outputConsumer;

	@BeforeMethod
	public void beforeMethod() {
		imageMagick = new ImageAnalyzerImp(SOME_TEMP_PATH);

		identifyCmd = new IdentifyCmdSpy();
		imOperation = new IMOperationSpy();
		outputConsumer = new ArrayListOutputConsumerSpy();

		ArrayList<String> returnedOutput = new ArrayList<>(List.of("72x72 2560 1440 3000"));
		outputConsumer.MRV.setDefaultReturnValuesSupplier("getOutput", () -> returnedOutput);

	}

	private void setUpSpies() {
		imageMagick.onlyForTestSetIdentifyCmd(identifyCmd);
		imageMagick.onlyForTestSetIMOperation(imOperation);
		imageMagick.onlyForTestSetArrayListOutputConsumer(outputConsumer);
	}

	@Test
	public void testAnalyzeImage() throws Exception {
		setUpSpies();

		ImageData imageData = imageMagick.analyze();

		assertEquals(imageData.resolution(), "72x72");
		assertEquals(imageData.width(), "2560");
		assertEquals(imageData.height(), "1440");
		assertEquals(imageData.size(), "3000");
	}

	@Test
	public void testMakeSureCallsToImOperationAreDoneInCorrectOrderAsItIsVital() throws Exception {
		setUpSpies();

		imageMagick.analyze();

		assertEquals(imOperation.callsInOrder.toString(), "[%xx%y %w %h %B, /someTempPath]");
	}

	@Test
	public void testAnalyzeImageCallsImageMagick() throws Exception {
		setUpSpies();

		imageMagick.analyze();

		String[] pathAsArray = (String[]) imOperation.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("addImage", 0, "arg0");

		assertEquals(pathAsArray[0], SOME_TEMP_PATH);
		imOperation.MCR.assertParameters("format", 0, FORMAT_DPI_WIDTH_HEIGHT_SIZE);

		identifyCmd.MCR.assertParameters("setOutputConsumer", 0, outputConsumer);
		identifyCmd.MCR.assertParameters("run", 0, imOperation);
		outputConsumer.MCR.methodWasCalled("getOutput");

	}

	@Test
	public void testAnalyzeThrowImageConverterException() throws Exception {
		setUpSpies();
		identifyCmd.MRV.setAlwaysThrowException("run", new RuntimeException("Error from spy"));

		try {
			imageMagick.analyze();
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
		setUpSpies();
		ArrayList<String> returnedOutput = new ArrayList<>(List.of("2560 1440"));
		outputConsumer.MRV.setDefaultReturnValuesSupplier("getOutput", () -> returnedOutput);

		try {
			imageMagick.analyze();
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof ImageConverterException);
			assertEquals(e.getMessage(),
					"Error when analyzing image, with path: " + SOME_TEMP_PATH);
			assertEquals(e.getCause().getMessage(), "Index 2 out of bounds for length 2");
		}
	}

	@Test
	public void testCheckRealVariablesInitialized() throws Exception {
		assertNotNull(imageMagick.identifyCmd);
		assertNotNull(imageMagick.imOperation);
		assertNotNull(imageMagick.outputConsumer);
	}

	@Test(enabled = false)
	public void testRealAnalyze() throws Exception {
		ImageAnalyzerImp imageMagickReal = new ImageAnalyzerImp(
				"/home/olov/workspace/IMG_20161005_130203.jpg");

		ImageData analyze = imageMagickReal.analyze();

		System.out.println("ImageData" + analyze);
	}

	@Test(enabled = false)
	public void testRealAnalyze2() throws Exception {
		ImageAnalyzerImp imageMagickReal = new ImageAnalyzerImp(
				"/home/olov/workspace/th-1561237634.jpg");

		ImageData analyze = imageMagickReal.analyze();

		System.out.println("ImageData" + analyze);
	}

	@Test(enabled = false)
	public void testRealAnalyze3() throws Exception {
		ImageAnalyzerImp imageMagickReal = new ImageAnalyzerImp(
				"/home/olov/workspace/KKH_D_002.tif");

		ImageData analyze = imageMagickReal.analyze();

		System.out.println("ImageData" + analyze);
	}

}
