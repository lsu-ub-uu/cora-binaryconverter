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
package se.uu.ub.cora.binaryconverter.imagemagick.document;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.MessageFormat;
import java.util.Optional;

import org.im4java.core.ConvertCmd;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.document.PdfConverter;
import se.uu.ub.cora.binaryconverter.imagemagick.BinaryOperationFactoryImp;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactory;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.ConvertCmdSpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IMOperationFactorySpy;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.IMOperationSpy;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;

public class PdfConverterTest {

	private static final String SOME_OUTPUT_PATH = "someOutputPath";
	private static final String SOME_INPUT_PATH = "someInputPath";
	private static final int SOME_WIDTH = 100;
	private static final String OUTPUT_FORMAT = "JPG:";
	private PdfConverterImp pdfConverter;
	private ConvertCmdSpy convertCmd;
	private IMOperationFactorySpy imOperationFactory;
	private BinaryOperationFactory binaryOperation;

	@BeforeMethod
	private void beforeMethod() {
		convertCmd = new ConvertCmdSpy();
		imOperationFactory = new IMOperationFactorySpy();
		binaryOperation = new BinaryOperationFactoryImp();

		pdfConverter = new PdfConverterImp(imOperationFactory, convertCmd);
	}

	@Test
	public void testImplementsPdfConverter() throws Exception {
		assertTrue(pdfConverter instanceof PdfConverter);
	}

	@Test
	public void testConvertPdf() throws Exception {

		pdfConverter.convertUsingWidth(SOME_INPUT_PATH, SOME_OUTPUT_PATH, SOME_WIDTH);

		imOperationFactory.MCR.assertParameters("factor", 0);
		IMOperationSpy imOperation = (IMOperationSpy) imOperationFactory.MCR
				.getReturnValue("factor", 0);
		assertFirstArgumentAddImage(imOperation, 0, SOME_INPUT_PATH + "[0]");
		imOperation.MCR.assertParameters("resize", 0, SOME_WIDTH);
		imOperation.MCR.assertParameterAsEqual("quality", 0, "var1", 90.0);
		imOperation.MCR.assertParameters("alpha", 0, "remove");
		assertFirstArgumentAddImage(imOperation, 1, OUTPUT_FORMAT + SOME_OUTPUT_PATH);

		convertCmd.MCR.assertParameters("run", 0, imOperation);

	}

	private void assertFirstArgumentAddImage(IMOperationSpy imOperation, int callNr, String value) {
		String[] arg = (String[]) imOperation.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("addImage", callNr, "arg0");
		assertEquals(arg[0], value);
	}

	@Test
	public void testInterrumpedException() throws Exception {
		convertCmd.throwInterruptException = Optional
				.of(new InterruptedException("someInterruptException"));

		try {
			pdfConverter.convertUsingWidth(SOME_INPUT_PATH, SOME_OUTPUT_PATH, SOME_WIDTH);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(Thread.currentThread().isInterrupted());

			assertTrue(e instanceof BinaryConverterException);
			assertException(e, "someInterruptException");
		}
	}

	private void assertException(Exception e, String thrownExceptionMessage) {
		String errorMsg = "Error creating first page thumbnail of a PDF on path {0} and width {1}";
		assertEquals(e.getMessage(), MessageFormat.format(errorMsg, SOME_INPUT_PATH, SOME_WIDTH));
		assertEquals(e.getCause().getMessage(), thrownExceptionMessage);
	}

	@Test
	public void testAnyExceptionInconvertUsingWidth() throws Exception {
		convertCmd.MRV.setAlwaysThrowException("run", new RuntimeException("someSpyException"));

		try {
			pdfConverter.convertUsingWidth(SOME_INPUT_PATH, SOME_OUTPUT_PATH, SOME_WIDTH);
			fail("It failed");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertException(e, "someSpyException");
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

		PdfConverter converter = binaryOperation.factorPdfConverter();
		converter.convertUsingWidth(input, output, 600);
	}

	@Test(enabled = false)
	public void testRealOlov() throws Exception {

		String input = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdf.pdf";
		String output = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputOlov_100.jpg";

		PdfConverter converter = binaryOperation.factorPdfConverter();
		converter.convertUsingWidth(input, output, 100);

		output = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputOlov_300.jpg";
		converter.convertUsingWidth(input, output, 300);

		output = "/home/olov/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aPdfOutputOlov_600.jpg";
		converter.convertUsingWidth(input, output, 600);
	}

}
