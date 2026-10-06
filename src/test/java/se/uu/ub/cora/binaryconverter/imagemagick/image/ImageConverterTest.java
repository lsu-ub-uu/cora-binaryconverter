/*
 * Copyright 2023, 2026 Uppsala University Library
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
package se.uu.ub.cora.binaryconverter.imagemagick.image;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.MessageFormat;
import java.util.Optional;

import org.im4java.core.ConvertCmd;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactory;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactoryImp;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.ConvertCmdSpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IMOperationFactorySpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IMOperationSpy;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

public class ImageConverterTest {
	private static final String SOME_TEMP_INPUT_PATH = "/someTempInputPath";
	private static final String SOME_TEMP_OUTPUT_PATH = "/someTempOutputPath";

	private ImageConverterImp imageConverter;
	private ConvertCmdSpy convertCmd;
	private IMOperationFactorySpy imOperationFactory;
	private String[] arguments;

	@BeforeMethod
	public void beforeMethod() {
		convertCmd = new ConvertCmdSpy();
		imOperationFactory = new IMOperationFactorySpy();

		imageConverter = new ImageConverterImp(imOperationFactory, convertCmd);
	}

	@Test
	public void testInit() {
		assertTrue(imageConverter instanceof ImageConverter);
	}

	@Test
	public void testConvertAndResizeUsingWidth_OK() {
		int width = 200;
		imageConverter.convertAndResizeUsingWidth(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH,
				width);

		imOperationFactory.MCR.assertParameters("factor", 0);
		IMOperationSpy imOperation = (IMOperationSpy) imOperationFactory.MCR
				.getReturnValue("factor", 0);

		assertFirstArgumentAddImage(imOperation, 0, SOME_TEMP_INPUT_PATH);
		imOperation.MCR.assertParameterAsEqual("addRawArgs", 0, "arg0",
				asArray("-colorspace", "sRGB"));
		imOperation.MCR.assertParameterAsEqual("addRawArgs", 1, "arg0",
				asArray("-background", "white", "-alpha", "remove", "-alpha", "off"));
		imOperation.MCR.assertParameterAsEqual("addRawArgs", 2, "arg0",
				asArray("-filter", "Lanczos"));
		imOperation.MCR.assertParameterAsEqual("addRawArgs", 3, "arg0",
				asArray("-resize", width + ">"));
		imOperation.MCR.assertParameterAsEqual("addRawArgs", 4, "arg0", asArray("-strip"));
		imOperation.MCR.assertParameterAsEqual("addRawArgs", 5, "arg0",
				asArray("-sampling-factor", "2x2"));

		imOperation.MCR.assertParameters("resize", 0, width);
		imOperation.MCR.assertParameterAsEqual("quality", 0, "var1", 90.0);
		assertFirstArgumentAddImage(imOperation, 1, "JPEG:" + SOME_TEMP_OUTPUT_PATH);

		convertCmd.MCR.assertParameters("run", 0, imOperation);
	}

	private void assertFirstArgumentAddImage(IMOperationSpy imOperation, int callNr, String value) {
		String[] arg = (String[]) imOperation.MCR
				.getParameterForMethodAndCallNumberAndParameter("addImage", callNr, "arg0");
		assertEquals(arg[0], value);
	}

	@Test
	public void testConvertAndResizeUsingWidth_Error() {
		convertCmd.MRV.setAlwaysThrowException("run", new RuntimeException("someSpyException"));

		int width = 100;

		try {
			imageConverter.convertAndResizeUsingWidth(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH,
					width);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			String errorMsg = "Error converting image on path {0} and width {1}";
			assertEquals(e.getMessage(),
					MessageFormat.format(errorMsg, SOME_TEMP_INPUT_PATH, width));
			assertEquals(e.getCause().getMessage(), "someSpyException");
		}
	}

	@Test(enabled = false)
	public void testRealConvert() {
		IMOperationFactory realImOperationFactory = new IMOperationFactoryImp();
		ConvertCmd realConvertCmd = new ConvertCmd();

		ImageConverterImp imageMagickReal = new ImageConverterImp(realImOperationFactory,
				realConvertCmd);

		imageMagickReal.convertAndResizeUsingWidth(
				"/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/sagradaFamilia.tiff",
				"/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/b", 600);
	}

	@Test
	public void testOnlyForTestGetImOperationFactory() {
		IMOperationFactory imOperationFactory1 = imageConverter.onlyForTestGetImOperationFactory();
		assertSame(imOperationFactory1, imOperationFactory);
	}

	@Test
	public void testOnlyForTestGetConvertCmd() {
		ConvertCmd convertCmd1 = imageConverter.onlyForTestGetConvertCmd();
		assertSame(convertCmd1, convertCmd);
	}

	@Test
	public void testInterrumpedException() {
		convertCmd.throwInterruptException = Optional
				.of(new InterruptedException("someInterruptException"));

		try {
			imageConverter.convertToTiff(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(Thread.currentThread().isInterrupted());

			assertTrue(e instanceof BinaryConverterException);
			assertException(e, "someInterruptException");
		}
	}

	private void assertException(Exception e, String thrownExceptionMessage) {
		String errorMsg = "Error converting image to TIFF on path {0}";
		assertEquals(e.getMessage(), MessageFormat.format(errorMsg, SOME_TEMP_INPUT_PATH));
		assertEquals(e.getCause().getMessage(), thrownExceptionMessage);
	}

	@Test
	public void testConvertToTiff() {

		imageConverter.convertToTiff(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH);

		imOperationFactory.MCR.assertParameters("factor", 0);
		IMOperationSpy imOperation = (IMOperationSpy) imOperationFactory.MCR
				.getReturnValue("factor", 0);

		assertFirstArgumentAddImage(imOperation, 0, SOME_TEMP_INPUT_PATH);
		imOperation.MCR.assertMethodNotCalled("resize");
		imOperation.MCR.assertMethodNotCalled("quality");
		assertFirstArgumentAddImage(imOperation, 1, "TIFF:" + SOME_TEMP_OUTPUT_PATH);

		convertCmd.MCR.assertParameters("run", 0, imOperation);
	}

	@Test
	public void testConvertToTiff_Error() {
		convertCmd.MRV.setAlwaysThrowException("run", new RuntimeException("someSpyException"));

		try {
			imageConverter.convertToTiff(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertException(e, "someSpyException");
		}
	}

	private String[] asArray(String... arguments) {
		return arguments;
	}
}
