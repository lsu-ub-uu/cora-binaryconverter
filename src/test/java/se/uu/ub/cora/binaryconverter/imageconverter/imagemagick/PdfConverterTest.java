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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.MessageFormat;

import org.im4java.core.ConvertCmd;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageConverterException;
import se.uu.ub.cora.binaryconverter.imageconverter.PdfConverter;
import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy.IMOperationSpy;
import se.uu.ub.cora.binaryconverter.spy.ConvertCmdSpy;
import se.uu.ub.cora.binaryconverter.spy.IMOperationFactorySpy;

public class PdfConverterTest {

	private static final String SOME_OUTPUT_PATH = "someOutputPath";
	private static final String SOME_INPUT_PATH = "someInputPath";
	private PdfConverterImp pdfConverter;
	private ConvertCmdSpy convertCmd;
	private IMOperationFactorySpy imOperationFactory;

	@BeforeMethod
	private void beforeMethod() {
		convertCmd = new ConvertCmdSpy();
		imOperationFactory = new IMOperationFactorySpy();

		pdfConverter = new PdfConverterImp(imOperationFactory, convertCmd);
	}

	@Test
	public void testImplementsPdfConverter() throws Exception {
		assertTrue(pdfConverter instanceof PdfConverter);
	}

	@Test
	public void testConvertPdf() throws Exception {
		int width = 100;

		pdfConverter.convertUsingWidth(SOME_INPUT_PATH, SOME_OUTPUT_PATH, width);

		imOperationFactory.MCR.assertParameters("factor", 0);
		IMOperationSpy imOperation = (IMOperationSpy) imOperationFactory.MCR
				.getReturnValue("factor", 0);
		assertFirstArgumentAddImage(imOperation, 0, SOME_INPUT_PATH + "[0]");
		imOperation.MCR.assertParameters("thumbnail", 0, width);
		imOperation.MCR.assertParameters("alpha", 0, "remove");
		assertFirstArgumentAddImage(imOperation, 1, SOME_OUTPUT_PATH);

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

		int width = 100;

		try {
			pdfConverter.convertUsingWidth(SOME_INPUT_PATH, SOME_OUTPUT_PATH, width);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof ImageConverterException);
			String errorMsg = "Error creating first page thumbnail of a PDF on path {0} and width {1}";
			assertEquals(e.getMessage(), MessageFormat.format(errorMsg, SOME_INPUT_PATH, width));
			assertEquals(e.getCause().getMessage(), "someSpyException");
		}
	}

	@Test
	public void testOnlyForTestGetImOperationFactory() throws Exception {
		IMOperationFactory imOperationFactory1 = pdfConverter.onlyForTestGetImOperationFactory();
		assertSame(imOperationFactory1, imOperationFactory);
	}

	@Test
	public void testOnlyForTestGetConvertCmd() throws Exception {
		ConvertCmd convertCmd1 = pdfConverter.onlyForTestGetConvertCmd();
		assertSame(convertCmd1, convertCmd);
	}

	@Test(enabled = false)
	public void testRealPere() throws Exception {

		String input = "/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdf.pdf";
		String output = "/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputSmall.jpg";

		PdfConverterFactoryImp converterFactory = new PdfConverterFactoryImp();
		PdfConverter converter = converterFactory.factor();
		converter.convertUsingWidth(input, output, 600);

		// output =
		// "/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputMedium.jpg";
		// pdfConverter = new PdfConverterImp(input, output, 300);
		// pdfConverter.convertUsingWidth(300);

		// output =
		// "/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputLarge.jpg";
		// pdfConverter = new PdfConverterImp(input, output, 600);
		// pdfConverter.convertUsingWidth(600);
	}

	@Test(enabled = false)
	public void testRealOlov() throws Exception {

		String input = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdf.pdf";
		String output = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputOlov_100.jpg";

		PdfConverterFactoryImp converterFactory = new PdfConverterFactoryImp();
		PdfConverter converter = converterFactory.factor();
		converter.convertUsingWidth(input, output, 100);

		output = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputOlov_300.jpg";
		converter.convertUsingWidth(input, output, 300);

		output = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputOlov_600.jpg";
		converter.convertUsingWidth(input, output, 600);
	}

}
