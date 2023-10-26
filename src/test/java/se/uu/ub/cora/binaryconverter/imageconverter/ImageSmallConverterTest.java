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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy.ImageAnalyzerFactorySpy;
import se.uu.ub.cora.binaryconverter.imageconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.messaging.MessageReceiver;

public class ImageSmallConverterTest {

	private static final String SOME_TYPE = "someType";
	private static final String SOME_ID = "someId";
	private static final String SOME_CHECKSUM = "d9f28c8b153ee8916c7f8faaa9d94bb04d06da7616034a4"
			+ "cd7e03102e30fa67cfa8eee1e7afbc7d3a5909285e41b24b16e08b2f7338d15398554407cf7025b45";
	private static final String SOME_MESSAGE = "someMessage";
	private static final String SOME_OCFL_HOME = "/someOcflRootHome";

	private ImageSmallConverter imageSmallConverter;
	private Map<String, String> some_headers = new HashMap<>();
	private ImageAnalyzerFactorySpy imageAnalyzerFactory;

	@BeforeMethod
	public void beforeMethod() {
		imageSmallConverter = new ImageSmallConverter(SOME_OCFL_HOME);
		imageAnalyzerFactory = new ImageAnalyzerFactorySpy();
		
		ClientDataF

		setMessageHeaders();
	}

	private void setMessageHeaders() {
		some_headers.put("type", SOME_TYPE);
		some_headers.put("id", SOME_ID);
		some_headers.put("checksum512", SOME_CHECKSUM);
	}

	@Test
	public void testImageAnalyzerFactoryInitialized() throws Exception {
		assertTrue(imageSmallConverter instanceof MessageReceiver);
		ImageAnalyzerFactory factory = imageSmallConverter.onlyForTestGetImageAnalyzerFactory();
		assertNotNull(factory);

	}

	@Test
	public void testCallFactoryWithCorrectPath() throws Exception {
		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);

		imageAnalyzerFactory.MCR.assertParameters("factor", 0, SOME_OCFL_HOME
				+ "/d9f/28c/8b1/53ee8916c7f8faaa9d94bb04d06da7616034a4cd7e03102e30fa67cfa8eee1e"
				+ "7afbc7d3a5909285e41b24b16e08b2f7338d15398554407cf7025b45/v1/content/"
				+ "someType:someId-master");
	}

	@Test
	public void testCallAnlayze() throws Exception {

		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) imageAnalyzerFactory.MCR
				.getReturnValue("factor", 0);

		analyzer.MCR.assertParameters("analyze", 0);

	}

}
