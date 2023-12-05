/*
 * Copyright 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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
package se.uu.ub.cora.binaryconverter.messagereceiver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.common.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.common.PathBuilder;
import se.uu.ub.cora.binaryconverter.common.PathBuilderImp;
import se.uu.ub.cora.binaryconverter.common.ResourceMetadataCreatorImp;
import se.uu.ub.cora.binaryconverter.document.PdfConverterFactory;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.image.ImageConverterFactory;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.JavaClientFactorySpy;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientProvider;
import se.uu.ub.cora.messaging.MessageReceiver;

public class MessageReceiverFactoryTest {
	private static final String IMAGE_CONVERTER_QUEUE = "smallConverterQueue";
	private static final String SOME_FILE_STORAGE_BASE_PATH = "/some/Base/Path/";
	private static final String SOME_APP_TOKEN_URL = "someAppTokenUrl";
	private static final String SOME_BASE_URL = "someBaseUrl";
	private static final String SOME_ARCHIVE_BASE_PATH = "/someOcfl/Home/Path/From/Fedora";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_USER_ID = "someUserId";
	private MessageReceiverFactory factory;
	private JavaClientFactorySpy javaClientFactory;
	private JavaClientAppTokenCredentials appTokenCredentials;

	@BeforeMethod
	public void beforeMethod() {
		javaClientFactory = new JavaClientFactorySpy();
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		appTokenCredentials = new JavaClientAppTokenCredentials(SOME_BASE_URL, SOME_APP_TOKEN_URL,
				SOME_USER_ID, SOME_APP_TOKEN);

		factory = new MessageReceiverFactoryImp();
	}

	@Test
	public void testFactorAnalayzeAndConvertImatgeToThumbnail() throws Exception {

		MessageReceiver messageReceiver = factory.factor(IMAGE_CONVERTER_QUEUE, appTokenCredentials,
				SOME_ARCHIVE_BASE_PATH, SOME_FILE_STORAGE_BASE_PATH);

		javaClientFactory.MCR.assertParameters("factorDataClientUsingJavaClientAppTokenCredentials",
				0, appTokenCredentials);

		assertMessageReceiver((AnalyzeAndConvertImageToThumbnails) messageReceiver);
		assertPathBuilder((AnalyzeAndConvertImageToThumbnails) messageReceiver);

		assertTrue(messageReceiver instanceof AnalyzeAndConvertImageToThumbnails);
	}

	private DataClientSpy getDataClientSpyFromeReturn() {
		return (DataClientSpy) javaClientFactory.MCR
				.getReturnValue("factorDataClientUsingJavaClientAppTokenCredentials", 0);
	}

	private void assertMessageReceiver(AnalyzeAndConvertImageToThumbnails messageReceiver) {
		assertEquals(messageReceiver.onlyForTestGetDataClient(), getDataClientSpyFromeReturn());
		assertTrue(messageReceiver
				.onlyForTestGetImageAnalyzerFactory() instanceof ImageAnalyzerFactory);
		assertTrue(messageReceiver
				.onlyForTestGetImageConverterFactory() instanceof ImageConverterFactory);
		assertTrue(messageReceiver
				.onlyForTestGetResourceMetadataCreator() instanceof ResourceMetadataCreatorImp);
		assertTrue(messageReceiver.onlyForTestGetPathBuilder() instanceof PathBuilder);
	}

	private void assertPathBuilder(AnalyzeAndConvertImageToThumbnails messageReceiver) {
		PathBuilderImp pathBuilder = (PathBuilderImp) messageReceiver.onlyForTestGetPathBuilder();
		assertEquals(pathBuilder.onlyForTestGetArchiveBasePath(), SOME_ARCHIVE_BASE_PATH);
		assertEquals(pathBuilder.onlyForTestGetFileSystemBasePath(), SOME_FILE_STORAGE_BASE_PATH);
		assertTrue(pathBuilder instanceof PathBuilderImp);
	}

	@Test
	public void testNotKnownQueueName() throws Exception {
		try {
			factory.factor("notKnownQueue", appTokenCredentials, SOME_ARCHIVE_BASE_PATH,
					SOME_FILE_STORAGE_BASE_PATH);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertEquals(e.getMessage(),
					"It could not start any message receiver with the queue name: notKnownQueue");
		}
	}

	@Test
	public void testFactorPdfConverterToThumbnails() throws Exception {
		ConvertPdfToThumbnails messageReceiver = (ConvertPdfToThumbnails) factory.factor(
				"pdfConverterQueue", appTokenCredentials, SOME_ARCHIVE_BASE_PATH,
				SOME_FILE_STORAGE_BASE_PATH);

		assertNotNull(messageReceiver);

		assertTrue(
				messageReceiver.onlyForTestGetPdfConverterFactory() instanceof PdfConverterFactory);
		assertTrue(messageReceiver
				.onlyForTestGetImageAnalyzerFactory() instanceof ImageAnalyzerFactory);
		assertEquals(messageReceiver.onlyForTestGetDataClient(), getDataClientSpyFromeReturn());
		assertTrue(messageReceiver
				.onlyForTestGetResourceMetadataCreator() instanceof ResourceMetadataCreatorImp);
		assertTrue(messageReceiver.onlyForTestGetPathBuilder() instanceof PathBuilder);

		PathBuilderImp pathBuilder = (PathBuilderImp) messageReceiver.onlyForTestGetPathBuilder();

		assertEquals(pathBuilder.onlyForTestGetArchiveBasePath(), SOME_ARCHIVE_BASE_PATH);
		assertEquals(pathBuilder.onlyForTestGetFileSystemBasePath(), SOME_FILE_STORAGE_BASE_PATH);
	}

}
