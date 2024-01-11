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

import org.im4java.core.ConvertCmd;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.imagemagick.document.PdfConverterImp;
import se.uu.ub.cora.binaryconverter.imagemagick.image.ImageAnalyzerImp;
import se.uu.ub.cora.binaryconverter.imagemagick.image.ImageConverterImp;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.openjpeg2.FilesWrapper;
import se.uu.ub.cora.binaryconverter.openjpeg2.Jp2ConverterUsingOpj2;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2CommandImp;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;

public class BinaryOperationFactoryTest {

	private BinaryOperationFactory factory;
	private static final String SOME_TEMP_PATH = "/someTempPath";

	@BeforeMethod
	private void beforeMethod() {
		factory = new BinaryOperationFactoryImp();
	}

	@Test
	public void testFactorImageAnalyzer() throws Exception {
		ImageAnalyzerImp imageAnalyzer = (ImageAnalyzerImp) factory
				.factorImageAnalyzer(SOME_TEMP_PATH);

		assertNotNull(imageAnalyzer);
		assertEquals(imageAnalyzer.onlyForTestGetImagePath(), SOME_TEMP_PATH);
	}

	@Test
	public void testFactorImageConverter() throws Exception {
		ImageConverterImp imageConverter = (ImageConverterImp) factory.factorImageConverter();

		assertNotNull(imageConverter);

		IMOperationFactory factory = imageConverter.onlyForTestGetImOperationFactory();
		assertTrue(factory instanceof IMOperationFactoryImp);

		ConvertCmd command = imageConverter.onlyForTestGetConvertCmd();
		assertNotNull(command);
	}

	@Test
	public void testFactorPdfConverter() throws Exception {
		PdfConverterImp pdfConverter = (PdfConverterImp) factory.factorPdfConverter();

		assertNotNull(pdfConverter);

		IMOperationFactory factory = pdfConverter.onlyForTestGetImOperationFactory();
		assertTrue(factory instanceof IMOperationFactoryImp);

		ConvertCmd command = pdfConverter.onlyForTestGetConvertCmd();
		assertNotNull(command);
	}

	// @Test
	// public void testFactorJp2Converter() throws Exception {
	// Jp2ConverterImp jp2Converter = (Jp2ConverterImp) factory.factorJp2Converter();
	//
	// assertNotNull(jp2Converter);
	//
	// IMOperationFactory factory = jp2Converter.onlyForTestGetImOperationFactory();
	// assertTrue(factory instanceof IMOperationFactoryImp);
	//
	// ConvertCmd command = jp2Converter.onlyForTestGetConvertCmd();
	// assertNotNull(command);
	// }
	@Test
	public void testFactorJp2Converter() throws Exception {
		Jp2ConverterUsingOpj2 jp2Converter = (Jp2ConverterUsingOpj2) factory.factorJp2Converter();

		assertNotNull(jp2Converter);

		Opj2CommandImp command = (Opj2CommandImp) jp2Converter.onlyForTestGetOpj2Command();
		assertNotNull(command);

		command.onlyForTestGetOpj2ProcessRunnerFactory();

		Opj2Parameters parameters = jp2Converter.onlyForTestGetOpj2Parameters();
		assertNotNull(parameters);

		ImageConverter imageConverter = jp2Converter.onlyForTestGetImageConverter();
		assertNotNull(imageConverter);

		FilesWrapper filesWrapper = jp2Converter.onlyForTestGetFilesWrapper();
		assertNotNull(filesWrapper);

	}

}
