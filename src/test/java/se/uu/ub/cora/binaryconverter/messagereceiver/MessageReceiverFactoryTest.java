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

import se.uu.ub.cora.basicstorage.path.StreamPathBuilderImp;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreatorImp;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.JavaClientFactorySpy;
import se.uu.ub.cora.fedoraarchive.path.ArchivePathBuilderImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientProvider;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.storage.StreamPathBuilder;
import se.uu.ub.cora.storage.archive.ArchivePathBuilder;

public class MessageReceiverFactoryTest {
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

		MessageReceiver messageReceiver = factory.factor("smallImageConverterQueue",
				appTokenCredentials, SOME_ARCHIVE_BASE_PATH, SOME_FILE_STORAGE_BASE_PATH);

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
				.onlyForTestGetBinaryOperationFactory() instanceof BinaryOperationFactory);
		assertTrue(messageReceiver
				.onlyForTestGetBinaryOperationFactory() instanceof BinaryOperationFactory);
		assertTrue(messageReceiver
				.onlyForTestGetResourceMetadataCreator() instanceof ResourceMetadataCreatorImp);
		assertTrue(
				messageReceiver.onlyForTestGetArchivePathBuilder() instanceof ArchivePathBuilder);
		assertTrue(messageReceiver.onlyForTestGetStreamPathBuilder() instanceof StreamPathBuilder);
	}

	private void assertPathBuilder(AnalyzeAndConvertImageToThumbnails messageReceiver) {
		ArchivePathBuilderImp archivePathBuilder = (ArchivePathBuilderImp) messageReceiver
				.onlyForTestGetArchivePathBuilder();
		assertArchivePathBuilder(archivePathBuilder);

		StreamPathBuilderImp streamPathBuilder = (StreamPathBuilderImp) messageReceiver
				.onlyForTestGetStreamPathBuilder();
		assertStreamPathBuilder(streamPathBuilder);
	}

	private void assertArchivePathBuilder(ArchivePathBuilderImp archivePathBuilder) {
		assertEquals(archivePathBuilder.onlyForTestGetArchiveBasePath(), SOME_ARCHIVE_BASE_PATH);
		assertTrue(archivePathBuilder instanceof ArchivePathBuilder);
	}

	private void assertStreamPathBuilder(StreamPathBuilderImp streamPathBuilder) {
		assertEquals(streamPathBuilder.onlyForTestGetFileSystemBasePath(),
				SOME_FILE_STORAGE_BASE_PATH);
		assertTrue(streamPathBuilder instanceof StreamPathBuilder);
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

		assertTrue(messageReceiver
				.onlyForTestGetBinaryOperationFactory() instanceof BinaryOperationFactory);
		assertEquals(messageReceiver.onlyForTestGetDataClient(), getDataClientSpyFromeReturn());
		assertTrue(messageReceiver
				.onlyForTestGetResourceMetadataCreator() instanceof ResourceMetadataCreatorImp);
		assertTrue(
				messageReceiver.onlyForTestGetArchivePathBuilder() instanceof ArchivePathBuilder);
		assertTrue(messageReceiver.onlyForTestGetStreamPathBuilder() instanceof StreamPathBuilder);

		assertPathBuilderUsingConvertPdfToThumbnails(messageReceiver);
	}

	private void assertPathBuilderUsingConvertPdfToThumbnails(
			ConvertPdfToThumbnails messageReceiver) {
		ArchivePathBuilderImp archivePathBuilder = (ArchivePathBuilderImp) messageReceiver
				.onlyForTestGetArchivePathBuilder();
		assertArchivePathBuilder(archivePathBuilder);

		StreamPathBuilderImp streamPathBuilder = (StreamPathBuilderImp) messageReceiver
				.onlyForTestGetStreamPathBuilder();
		assertStreamPathBuilder(streamPathBuilder);
	}

	@Test
	public void testFactorImageConverterToJp2() throws Exception {
		ConvertImageToJp2 messageReceiver = (ConvertImageToJp2) factory.factor("jp2ConverterQueue",
				appTokenCredentials, SOME_ARCHIVE_BASE_PATH, SOME_FILE_STORAGE_BASE_PATH);

		assertNotNull(messageReceiver);

		assertTrue(messageReceiver
				.onlyForTestGetBinaryOperationFactory() instanceof BinaryOperationFactory);
		assertEquals(messageReceiver.onlyForTestGetDataClient(), getDataClientSpyFromeReturn());
		assertTrue(messageReceiver
				.onlyForTestGetResourceMetadataCreator() instanceof ResourceMetadataCreatorImp);
		assertTrue(
				messageReceiver.onlyForTestGetArchivePathBuilder() instanceof ArchivePathBuilder);
		assertTrue(messageReceiver.onlyForTestGetStreamPathBuilder() instanceof StreamPathBuilder);

		assertPathBuilderUsingConvertImageToJp2(messageReceiver);
	}

	private void assertPathBuilderUsingConvertImageToJp2(ConvertImageToJp2 messageReceiver) {
		ArchivePathBuilderImp archivePathBuilder = (ArchivePathBuilderImp) messageReceiver
				.onlyForTestGetArchivePathBuilder();
		assertArchivePathBuilder(archivePathBuilder);

		StreamPathBuilderImp streamPathBuilder = (StreamPathBuilderImp) messageReceiver
				.onlyForTestGetStreamPathBuilder();
		assertStreamPathBuilder(streamPathBuilder);
	}

}
