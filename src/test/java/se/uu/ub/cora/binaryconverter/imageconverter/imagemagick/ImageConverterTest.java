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
package se.uu.ub.cora.binaryconverter.imageconverter.imagemagick;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy.ArrayListOutputConsumerSpy;
import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy.IMOperationSpy;
import se.uu.ub.cora.binaryconverter.spy.ConvertCmdSpy;

public class ImageConverterTest {

	private static final String FORMAT_DPI_WIDTH_HEIGHT = "%xx%y %w %h";
	private static final String SOME_TEMP_INPUT_PATH = "/someTempInputPath";
	private static final String SOME_TEMP_OUTPUT_PATH = "/someTempOutputPath";
	ImageConverterImp converter;

	private IMOperationSpy imOperation;
	private ArrayListOutputConsumerSpy outputConsumer;
	private ConvertCmdSpy convertCmd;

	@BeforeMethod
	public void beforeMethod() {
		converter = new ImageConverterImp(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH);

		convertCmd = new ConvertCmdSpy();
		imOperation = new IMOperationSpy();
		// outputConsumer = new ArrayListOutputConsumerSpy();

		// ArrayList<String> returnedOutput = new ArrayList<>(List.of("72x72 2560 1440"));
		// outputConsumer.MRV.setDefaultReturnValuesSupplier("getOutput", () -> returnedOutput);

	}

	private void setUpSpies() {
		converter.onlyForTestSetConvertCmd(convertCmd);
		converter.onlyForTestSetIMOperation(imOperation);
		converter.onlyForTestSetArrayListOutputConsumer(outputConsumer);
	}

	@Test
	public void testConvertImage() throws Exception {
		setUpSpies();

		converter.convertToThumbnail();

		String[] input = (String[]) imOperation.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("addImage", 0, "arg0");
		assertEquals(input[0], SOME_TEMP_INPUT_PATH);

		assertResizeHeight(100);
		assertQualitySetTo(100.0);

		String[] output = (String[]) imOperation.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("addImage", 1, "arg0");
		assertEquals(output[0], SOME_TEMP_OUTPUT_PATH);

		convertCmd.MCR.assertParameters("run", 0, imOperation);

	}

	private void assertResizeHeight(int expectedHeight) {
		var width = imOperation.MCR.getValueForMethodNameAndCallNumberAndParameterName("resize", 0,
				"var1");
		var height = imOperation.MCR.getValueForMethodNameAndCallNumberAndParameterName("resize", 0,
				"var2");

		assertNull(width);
		assertEquals(height, expectedHeight);
	}

	private void assertQualitySetTo(double quality) {
		imOperation.MCR.assertParameterAsEqual("quality", 0, "var1", quality);
	}

	@Test(enabled = true)
	public void testRealAnalyze2() throws Exception {
		ImageConverterImp imageMagickReal = new ImageConverterImp(
				"/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/sagradaFamilia.tiff",
				"/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/thumbnail.jpg");

		imageMagickReal.convertToThumbnail();

	}

	// @Test
	// public void testAnalyzeImage() throws Exception {
	// setUpSpies();
	//
	// ImageData imageData = converter.analyze();
	//
	// assertEquals(imageData.resolution(), "72x72");
	// assertEquals(imageData.width(), "2560");
	// assertEquals(imageData.height(), "1440");
	// }
	//
	// @Test
	// public void testMakeSureCallsToImOperationAreDoneInCorrectOrderAsItIsVital() throws Exception
	// {
	// setUpSpies();
	//
	// converter.analyze();
	//
	// assertEquals(imOperation.callsInOrder.toString(), "[%xx%y %w %h, /someTempPath]");
	// }
	//
	// @Test
	// public void testAnalyzeImageCallsImageMagick() throws Exception {
	// setUpSpies();
	//
	// converter.analyze();
	//
	// String[] pathAsArray = (String[]) imOperation.MCR
	// .getValueForMethodNameAndCallNumberAndParameterName("addImage", 0, "arg0");
	//
	// assertEquals(pathAsArray[0], SOME_TEMP_PATH);
	// imOperation.MCR.assertParameters("format", 0, FORMAT_DPI_WIDTH_HEIGHT);
	//
	// identifyCmd.MCR.assertParameters("setOutputConsumer", 0, outputConsumer);
	// identifyCmd.MCR.assertParameters("run", 0, imOperation);
	// outputConsumer.MCR.methodWasCalled("getOutput");
	//
	// }
	//
	// @Test
	// public void testAnalyzeThrowImageConverterException() throws Exception {
	// setUpSpies();
	// identifyCmd.MRV.setAlwaysThrowException("run", new RuntimeException("Error from spy"));
	//
	// try {
	// converter.analyze();
	// fail("It failed");
	// } catch (Exception e) {
	// assertTrue(e instanceof ImageConverterException);
	// assertEquals(e.getMessage(),
	// "Error when analyzing image, with path: " + SOME_TEMP_PATH);
	// assertEquals(e.getCause().getMessage(), "Error from spy");
	// }
	// }
	//
	// @Test
	// public void testAnalyzeImageMagickReturnLessValuesThanExpected() throws Exception {
	// setUpSpies();
	// ArrayList<String> returnedOutput = new ArrayList<>(List.of("2560 1440"));
	// outputConsumer.MRV.setDefaultReturnValuesSupplier("getOutput", () -> returnedOutput);
	//
	// try {
	// converter.analyze();
	// fail("It failed");
	// } catch (Exception e) {
	// assertTrue(e instanceof ImageConverterException);
	// assertEquals(e.getMessage(),
	// "Error when analyzing image, with path: " + SOME_TEMP_PATH);
	// assertEquals(e.getCause().getMessage(), "Index 2 out of bounds for length 2");
	// }
	// }
	//
	// @Test
	// public void testCheckRealVariablesInitialized() throws Exception {
	// assertNotNull(converter.identifyCmd);
	// assertNotNull(converter.imOperation);
	// assertNotNull(converter.outputConsumer);
	// }
	//
	// @Test
	// public void testConvertToThumbnail() throws Exception {
	// // convert space.jpg -resize x100 -quality 100 output.jpg
	//
	// converter.convertToThumbnail();
	// }
	//
	// @Test(enabled = false)
	// public void testRealAnalyze() throws Exception {
	// ImageConverterImp imageMagickReal = new ImageConverterImp(
	// "/home/olov/workspace/IMG_20161005_130203.jpg");
	//
	// ImageData analyze = imageMagickReal.analyze();
	//
	// System.out.println("ImageData" + analyze);
	// }
	//
	// @Test(enabled = false)
	// public void testRealAnalyze2() throws Exception {
	// ImageConverterImp imageMagickReal = new ImageConverterImp(
	// "/home/olov/workspace/th-1561237634.jpg");
	//
	// ImageData analyze = imageMagickReal.analyze();
	//
	// System.out.println("ImageData" + analyze);
	// }
	//
	// @Test(enabled = false)
	// public void testRealAnalyze3() throws Exception {
	// ImageConverterImp imageMagickReal = new ImageConverterImp(
	// "/home/olov/workspace/KKH_D_002.tif");
	//
	// ImageData analyze = imageMagickReal.analyze();
	//
	// System.out.println("ImageData" + analyze);
	// }

}
