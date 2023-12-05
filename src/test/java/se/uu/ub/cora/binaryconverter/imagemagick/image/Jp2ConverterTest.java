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
package se.uu.ub.cora.binaryconverter.imagemagick.image;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.MessageFormat;

import org.im4java.core.ConvertCmd;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.common.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactory;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactoryImp;
import se.uu.ub.cora.binaryconverter.imagemagick.image.Jp2ConverterImp;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.ConvertCmdSpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IMOperationFactorySpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IMOperationSpy;

public class Jp2ConverterTest {
	private static final String SOME_TEMP_INPUT_PATH = "/someTempInputPath";
	private static final String SOME_TEMP_OUTPUT_PATH = "/someTempOutputPath";

	private Jp2ConverterImp jp2Converter;
	private ConvertCmdSpy convertCmd;
	private IMOperationFactorySpy imOperationFactory;

	@BeforeMethod
	public void beforeMethod() {
		convertCmd = new ConvertCmdSpy();
		imOperationFactory = new IMOperationFactorySpy();

		jp2Converter = new Jp2ConverterImp(imOperationFactory, convertCmd);
	}

	@Test
	public void testConvertImage() throws Exception {
		jp2Converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH);

		imOperationFactory.MCR.assertParameters("factor", 0);
		IMOperationSpy imOperation = (IMOperationSpy) imOperationFactory.MCR
				.getReturnValue("factor", 0);

		assertFirstArgumentAddImage(imOperation, 0, SOME_TEMP_INPUT_PATH);

		imOperation.MCR.assertParameterAsEqual("define", 0, "arg0", "jp2:progression-order=RPCL");
		imOperation.MCR.assertParameterAsEqual("define", 1, "arg0", "jp2:quality=25,28,30,35,40");
		imOperation.MCR.assertParameterAsEqual("define", 2, "arg0", "jp2:prcwidth=256");
		imOperation.MCR.assertParameterAsEqual("define", 3, "arg0", "jp2:prcheight=256");
		imOperation.MCR.assertParameterAsEqual("define", 4, "arg0", "jp2:cblkwidth=64");
		imOperation.MCR.assertParameterAsEqual("define", 5, "arg0", "jp2:cblkheight=64");
		imOperation.MCR.assertParameterAsEqual("define", 6, "arg0", "jp2:sop");
		imOperation.MCR.assertParameterAsEqual("define", 7, "arg0", "jp2:eph");

		assertFirstArgumentAddImage(imOperation, 1, "JP2:" + SOME_TEMP_OUTPUT_PATH);

		convertCmd.MCR.assertParameters("run", 0, imOperation);
	}

	private void assertFirstArgumentAddImage(IMOperationSpy imOperation, int callNr, String value) {
		String[] arg = (String[]) imOperation.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("addImage", callNr, "arg0");
		assertEquals(arg[0], value);
	}

	@Test
	public void testError() throws Exception {
		convertCmd.MRV.setAlwaysThrowException("run", new RuntimeException("someSpyException"));

		try {
			jp2Converter.convert(SOME_TEMP_INPUT_PATH, SOME_TEMP_OUTPUT_PATH);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			String errorMsg = "Error converting to Jpeg2000 image on path {0}";
			assertEquals(e.getMessage(), MessageFormat.format(errorMsg, SOME_TEMP_INPUT_PATH));
			assertEquals(e.getCause().getMessage(), "someSpyException");
		}
	}

	@Test(enabled = false)
	public void testRealConvert() throws Exception {
		IMOperationFactory realImOperationFactory = new IMOperationFactoryImp();
		ConvertCmd realConvertCmd = new ConvertCmd();

		Jp2ConverterImp imageMagickReal = new Jp2ConverterImp(realImOperationFactory,
				realConvertCmd);

		imageMagickReal.convert(
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/sagradaFamilia.tiff",
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/b.jp2");
	}

	@Test
	public void testOnlyForTestGetImOperationFactory() throws Exception {
		IMOperationFactory imOperationFactory1 = jp2Converter.onlyForTestGetImOperationFactory();
		assertSame(imOperationFactory1, imOperationFactory);
	}

	@Test
	public void testOnlyForTestGetConvertCmd() throws Exception {
		jp2Converter.onlyForTestSetConvertCmd(convertCmd);
		ConvertCmd convertCmd1 = jp2Converter.onlyForTestGetConvertCmd();
		assertSame(convertCmd1, convertCmd);
	}
}
