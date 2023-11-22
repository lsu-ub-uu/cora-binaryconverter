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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.im4java.core.ConvertCmd;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ImageConverterFactoryTest {

	private PdfConverterFactory factory;

	@BeforeMethod
	private void beforeMethod() {
		factory = new PdfConverterFactoryImp();
	}

	@Test
	public void testFactor() throws Exception {
		PdfConverterImp pdfConverter = (PdfConverterImp) factory.factor();

		assertNotNull(pdfConverter);
	}

	@Test
	public void testDependencies_IMOperationFactory() throws Exception {
		PdfConverterImp pdfConverter = (PdfConverterImp) factory.factor();

		IMOperationFactory factory = pdfConverter.onlyForTestGetImOperationFactory();
		assertTrue(factory instanceof IMOperationFactoryImp);
	}

	@Test
	public void testDependencies_() throws Exception {
		PdfConverterImp pdfConverter = (PdfConverterImp) factory.factor();

		ConvertCmd command = pdfConverter.onlyForTestGetConvertCmd();
		assertNotNull(command);
	}
}
