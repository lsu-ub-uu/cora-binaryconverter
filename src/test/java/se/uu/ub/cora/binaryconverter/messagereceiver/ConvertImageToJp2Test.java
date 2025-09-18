/*
 * Copyright 2023, 2024 Uppsala University Library
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
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.spy.BinaryOperationFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.binaryconverter.spy.Jp2ConverterSpy;
import se.uu.ub.cora.binaryconverter.spy.ResourceMetadataCreatorSpy;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.logger.spies.LoggerSpy;
import se.uu.ub.cora.storage.spies.path.ArchivePathBuilderSpy;
import se.uu.ub.cora.storage.spies.path.StreamPathBuilderSpy;

public class ConvertImageToJp2Test {
	private static final String JP2_MIME_TYPE = "image/jp2";
	private static final String DATA_DIVIDER = "someDataDivider";
	private static final String TYPE = "someType";
	private static final String ID = "someId";
	private static final String MIME_TYPE = "someMimeType";
	private static final String MESSAGE = "someMessage";

	private LoggerFactorySpy loggerFactorySpy;
	private Map<String, String> someHeaders = new HashMap<>();
	private ClientDataFactorySpy clientDataFactory;
	private DataClientSpy dataClient;
	private BinaryOperationFactorySpy binaryOperationFactory;
	private ArchivePathBuilderSpy archivePathBuilder;
	private StreamPathBuilderSpy streamPathBuilder;
	private ResourceMetadataCreatorSpy resourceMetadataCreator;

	private ConvertImageToJp2 messageReceiver;
	private LoggerSpy logger;

	@BeforeMethod
	public void beforeMethod() {
		logger = new LoggerSpy();
		loggerFactorySpy = new LoggerFactorySpy();
		loggerFactorySpy.MRV.setDefaultReturnValuesSupplier("factorForClass", () -> logger);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataClient = new DataClientSpy();
		binaryOperationFactory = new BinaryOperationFactorySpy();

		archivePathBuilder = new ArchivePathBuilderSpy();
		streamPathBuilder = new StreamPathBuilderSpy();

		resourceMetadataCreator = new ResourceMetadataCreatorSpy();

		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);

		messageReceiver = new ConvertImageToJp2(binaryOperationFactory, dataClient,
				resourceMetadataCreator, archivePathBuilder, streamPathBuilder);

		setMessageHeaders();
	}

	private void setMessageHeaders() {
		someHeaders.put("dataDivider", DATA_DIVIDER);
		someHeaders.put("type", TYPE);
		someHeaders.put("id", ID);
		someHeaders.put("mimeType", MIME_TYPE);
	}

	@Test
	public void testLoggerStarted() {
		loggerFactorySpy.MCR.assertParameters("factorForClass", 0, ConvertImageToJp2.class);
	}

	@Test
	public void testConvertImageToJp2Called() {
		messageReceiver.receiveMessage(someHeaders, MESSAGE);

		String resourceMasterPath = (String) archivePathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		assertAnalyzeAndConvertToRepresentation("jp2", resourceMasterPath, 0);
	}

	@Test
	public void testConvertAndAnalyzeAndUpdateAllRepresentations() {
		messageReceiver.receiveMessage(someHeaders, MESSAGE);

		binaryOperationFactory.MCR.assertNumberOfCallsToMethod("factorImageAnalyzer", 1);
		var image = getImageData(0);

		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 0,
				"somePathToAFile", ID, "jp2", JP2_MIME_TYPE, image);

		var jp2G = resourceMetadataCreator.MCR.getReturnValue("createMetadataForRepresentation", 0);

		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();

		binaryRecordGroup.MCR.assertParameters("addChild", 0, jp2G);
	}

	private ImageData getImageData(int callNr) {
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", callNr);
		return (ImageData) imageAnalyzer.MCR.getReturnValue("analyze", 0);
	}

	private void assertAnalyzeAndConvertToRepresentation(String representation, String inputPath,
			int callNr) {
		String pathToFileRepresentation = assertConvertToRepresentation(representation, inputPath,
				callNr);
		assertAnalyzeRepresentation(callNr, pathToFileRepresentation);
	}

	private String assertConvertToRepresentation(String representation, String inputPath,
			int callNr) {
		String pathToFileRepresentation = assertStreamPathBuilderBuildFileSystemFilePath(
				representation, callNr);
		assertCallToConvert(inputPath, callNr);
		return pathToFileRepresentation;
	}

	private void assertCallToConvert(String inputPath, int callNr) {
		binaryOperationFactory.MCR.assertParameters("factorJp2Converter", callNr);
		Jp2ConverterSpy jp2Converter = (Jp2ConverterSpy) binaryOperationFactory.MCR
				.getReturnValue("factorJp2Converter", callNr);
		jp2Converter.MCR.assertParameters("convert", 0, inputPath, "somePathToAFile", MIME_TYPE);
	}

	private String assertStreamPathBuilderBuildFileSystemFilePath(String representation,
			int callNr) {
		streamPathBuilder.MCR.assertParameters("buildPathToAFileAndEnsureFolderExists", callNr,
				DATA_DIVIDER, TYPE, ID, representation);
		return (String) streamPathBuilder.MCR
				.getReturnValue("buildPathToAFileAndEnsureFolderExists", callNr);
	}

	private void assertAnalyzeRepresentation(int callNr, String pathToFileRepresentation) {
		binaryOperationFactory.MCR.assertParameters("factorImageAnalyzer", callNr,
				pathToFileRepresentation);
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", callNr);
		imageAnalyzer.MCR.assertParameters("analyze", 0);
	}

	@Test
	public void testUpdateRecord() {
		messageReceiver.receiveMessage(someHeaders, MESSAGE);

		dataClient.MCR.assertParameters("read", 0, TYPE, ID);

		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();

		dataClient.MCR.assertParameters("update", 0, TYPE, ID, binaryRecordGroup);
	}

	private ClientDataRecordGroupSpy getBinaryRecordGroup() {
		ClientDataRecordSpy dataRecord = (ClientDataRecordSpy) dataClient.MCR.getReturnValue("read",
				0);
		dataRecord.MCR.assertParameters("getDataRecordGroup", 0);
		return (ClientDataRecordGroupSpy) dataRecord.MCR.getReturnValue("getDataRecordGroup", 0);
	}

	@Test
	public void testUpdateReturn_Conflict_409() {
		DataClientException conflictException = DataClientException
				.withMessageAndResponseCode("someConflictError", 409);

		Supplier<?> supplierThrowConflictExceptionOnFirstCall = () -> {
			return throwConflictExceptionOnFirstCall(conflictException);
		};
		dataClient.MRV.setDefaultReturnValuesSupplier("update",
				supplierThrowConflictExceptionOnFirstCall);

		messageReceiver.receiveMessage(someHeaders, MESSAGE);

		dataClient.MCR.assertNumberOfCallsToMethod("read", 2);
		dataClient.MCR.assertNumberOfCallsToMethod("update", 2);
		logger.MCR.assertParameters("logInfoUsingMessage", 0, "Binary record with id: " + ID
				+ " could not be updated due to record conflict. Retrying record update.");
	}

	int supplierCount = 0;

	private Object throwConflictExceptionOnFirstCall(DataClientException conflictException) {
		supplierCount++;
		if (supplierCount == 1) {
			throw conflictException;
		}
		return new ClientDataRecordSpy();

	}

	@Test
	public void testUpdateReturn_AnyOtherExceptionWithResponseCode() {
		DataClientException conflictException = DataClientException
				.withMessageAndResponseCode("someConflictError", 401);

		dataClient.MRV.setAlwaysThrowException("update", conflictException);
		try {
			messageReceiver.receiveMessage(someHeaders, MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertEquals(e.getMessage(), "Binary record with id: " + ID
					+ " could not be updated with jp2 conversion data.");
			assertEquals(e.getCause(), conflictException);
		}
	}

	@Test
	public void testUpdateReturn_AnyOtherExceptionWithoutResponseCode() {
		DataClientException conflictException = DataClientException
				.withMessage("someConflictError");

		dataClient.MRV.setAlwaysThrowException("update", conflictException);
		try {
			messageReceiver.receiveMessage(someHeaders, MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertEquals(e.getMessage(), "Binary record with id: " + ID
					+ " could not be updated with jp2 conversion data.");
			assertEquals(e.getCause(), conflictException);
		}
	}

	@Test
	public void testOnlyForTestGet() {
		assertEquals(messageReceiver.onlyForTestGetDataClient(), dataClient);
		assertEquals(messageReceiver.onlyForTestGetBinaryOperationFactory(),
				binaryOperationFactory);
		assertEquals(messageReceiver.onlyForTestGetArchivePathBuilder(), archivePathBuilder);
		assertEquals(messageReceiver.onlyForTestGetResourceMetadataCreator(),
				resourceMetadataCreator);
	}

	@Test
	public void testTopicClosed() {
		messageReceiver.topicClosed();
		LoggerSpy loggerSpy = (LoggerSpy) loggerFactorySpy.MCR.getReturnValue("factorForClass", 0);

		loggerSpy.MCR.assertParameters("logFatalUsingMessage", 0, "Topic is closed!");
	}
}
