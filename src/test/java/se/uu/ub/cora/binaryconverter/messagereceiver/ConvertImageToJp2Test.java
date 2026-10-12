/*
 * Copyright 2023, 2024, 2026 Uppsala University Library
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.spy.BinaryOperationFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.binaryconverter.spy.Jp2ConverterSpy;
import se.uu.ub.cora.binaryconverter.spy.ResourceMetadataCreatorSpy;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataAtomicSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
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
	private Map<String, String> headers = new HashMap<>();
	private ClientDataFactorySpy clientDataFactory;
	private DataClientSpy dataClient;
	private BinaryOperationFactorySpy binaryOperationFactory;
	private ArchivePathBuilderSpy archivePathBuilder;
	private StreamPathBuilderSpy streamPathBuilder;
	private ResourceMetadataCreatorSpy resourceMetadataCreator;
	private ClientDataRecordSpy dataRecord;
	private ClientDataRecordGroupSpy binaryRecordGroup;
	private ClientDataGroupSpy recordInfo;

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

		setReadRecordOnStorage();
	}

	@AfterMethod
	private void afterMethod() {
		supplierCount = 0;
	}

	private void setReadRecordOnStorage() {
		dataRecord = new ClientDataRecordSpy();
		binaryRecordGroup = new ClientDataRecordGroupSpy();
		recordInfo = new ClientDataGroupSpy();

		dataRecord.MRV.setDefaultReturnValuesSupplier("getDataRecordGroup",
				() -> binaryRecordGroup);
		binaryRecordGroup.MRV.setSpecificReturnValuesSupplier("getFirstChildOfTypeAndName",
				() -> recordInfo, ClientDataGroup.class, "recordInfo");

		dataClient.MRV.setDefaultReturnValuesSupplier("read", () -> dataRecord);
	}

	private void setMessageHeaders() {
		headers.put("dataDivider", DATA_DIVIDER);
		headers.put("type", TYPE);
		headers.put("id", ID);
		headers.put("mimeType", MIME_TYPE);
	}

	@Test
	public void testLoggerStarted() {
		loggerFactorySpy.MCR.assertParameters("factorForClass", 0, ConvertImageToJp2.class);
	}

	@Test
	public void testConvertImageToJp2Called() {
		messageReceiver.receiveMessage(headers, MESSAGE);

		String resourceMasterPath = (String) archivePathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		assertAnalyzeAndConvertToRepresentation("jp2", resourceMasterPath, 0);
	}

	@Test
	public void testConvertAndAnalyzeAndUpdateAllRepresentations() {
		messageReceiver.receiveMessage(headers, MESSAGE);

		binaryOperationFactory.MCR.assertNumberOfCallsToMethod("factorImageAnalyzer", 1);
		var image = getImageData(0);

		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 0,
				"somePathToAFile", ID, "jp2", JP2_MIME_TYPE, image);

		var jp2G = resourceMetadataCreator.MCR.getReturnValue("createMetadataForRepresentation", 0);

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
	public void testSetStatusToProcessing() {
		setUpStatusOnReadRecordFromStorage("uploaded");

		messageReceiver.receiveMessage(headers, MESSAGE);

		recordInfo.MCR.assertParameters("getFirstAtomicValueWithNameInData", 0, "status");
		assertSetStatus("processing");
	}

	private void assertSetStatus(String status) {
		recordInfo.MCR.assertCalledParameters("removeFirstChildWithTypeAndName",
				ClientDataAtomic.class, "status");
		ClientDataAtomicSpy statusAtomic = (ClientDataAtomicSpy) clientDataFactory.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "status",
						status);
		recordInfo.MCR.assertCalledParameters("addChild", statusAtomic);
	}

	private void setUpStatusOnReadRecordFromStorage(String status) {
		recordInfo.MRV.setSpecificReturnValuesSupplier("getFirstAtomicValueWithNameInData",
				() -> status, "status");
	}

	@Test
	public void testSetStatusToDone() {
		setUpStatusOnReadRecordFromStorage("processing");

		messageReceiver.receiveMessage(headers, MESSAGE);

		recordInfo.MCR.assertParameters("getFirstAtomicValueWithNameInData", 0, "status");
		assertSetStatus("done");
	}

	@Test
	public void testUpdateRecord() {
		messageReceiver.receiveMessage(headers, MESSAGE);

		dataClient.MCR.assertParameters("read", 0, TYPE, ID);

		dataClient.MCR.assertParameters("update", 0, TYPE, ID, binaryRecordGroup);
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

		messageReceiver.receiveMessage(headers, MESSAGE);

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
		DataClientException someException = DataClientException
				.withMessageAndResponseCode("someError2", 666);

		dataClient.MRV.setAlwaysThrowException("update", someException);

		messageReceiver.receiveMessage(headers, MESSAGE);

		assertLogWhenException(someException);
		assertSetStatus("failed");
	}

	private void assertLogWhenException(DataClientException conflictException) {
		logger.MCR.assertNumberOfCallsToMethod("logErrorUsingMessageAndException", 2);

		logger.MCR.assertParameter("logErrorUsingMessageAndException", 0, "message",
				"Error while converting with type: someType, id: someId, "
						+ "dataDivider: someDataDivider and mimeType: someMimeType.");

		RuntimeException loggedException = (RuntimeException) logger.MCR
				.getParameterForMethodAndCallNumberAndParameter("logErrorUsingMessageAndException",
						0, "exception");

		assertTrue(loggedException instanceof BinaryConverterException);
		assertEquals(loggedException.getMessage(),
				"Binary record with id: someId could not be updated with jp2 conversion data.");

		assertSame(loggedException.getCause(), conflictException);
	}

	@Test
	public void testUpdateReturn_AnyOtherExceptionWithoutResponseCode() {
		DataClientException someException = DataClientException.withMessage("someError");

		dataClient.MRV.setAlwaysThrowException("update", someException);

		messageReceiver.receiveMessage(headers, MESSAGE);

		assertLogWhenException(someException);
		assertSetStatus("failed");
	}

	@Test
	public void testLoggErrorsWhenErrorOccursOnConvertionRetryUpdateIfConflict() {
		RuntimeException exception = new RuntimeException();
		archivePathBuilder.MRV.setAlwaysThrowException("buildPathToAResourceInArchive", exception);

		DataClientException conflictException = DataClientException
				.withMessageAndResponseCode("someConflictError", 409);

		Supplier<?> supplierThrowConflictExceptionOnFirstCall = () -> {
			return throwConflictExceptionOnFirstCall(conflictException);
		};
		dataClient.MRV.setDefaultReturnValuesSupplier("update",
				supplierThrowConflictExceptionOnFirstCall);

		messageReceiver.receiveMessage(headers, MESSAGE);

		dataClient.MCR.assertNumberOfCallsToMethod("update", 2);
	}

	@Test
	public void testLoggErrorsWhenErrorOccursOnConvertionRetryUpdateIfConflictAndThenAnotherException() {
		RuntimeException exception = new RuntimeException();
		archivePathBuilder.MRV.setAlwaysThrowException("buildPathToAResourceInArchive", exception);

		DataClientException someException = DataClientException.withMessage("someError");
		dataClient.MRV.setAlwaysThrowException("update", someException);

		messageReceiver.receiveMessage(headers, MESSAGE);

		logger.MCR.assertNumberOfCallsToMethod("logErrorUsingMessageAndException", 2);
		assertLoggedError(0,
				"Error while converting with type: someType, id: someId, dataDivider: someDataDivider "
						+ "and mimeType: someMimeType.",
				RuntimeException.class);
		assertLoggedError(1, "Error while converting.", BinaryConverterException.class);

	}

	private void assertLoggedError(int callNumber, String expectedMessage,
			Class<? extends Throwable> expectedExceptionType) {

		logger.MCR.assertParameter("logErrorUsingMessageAndException", callNumber, "message",
				expectedMessage);

		var loggedException = logger.MCR.getParameterForMethodAndCallNumberAndParameter(
				"logErrorUsingMessageAndException", callNumber, "exception");

		assertTrue(expectedExceptionType.isInstance(loggedException),
				"Expected logged exception to be an instance of "
						+ expectedExceptionType.getSimpleName());
	}

	@Test
	public void testProtectionToAvoidRepublishMessagesIntoRabbit() {
		messageReceiver.receiveMessage(null, null);

		logger.MCR.assertParameter("logErrorUsingMessageAndException", 0, "message",
				"Error while converting.");

		var exception = logger.MCR.getParameterForMethodAndCallNumberAndParameter(
				"logErrorUsingMessageAndException", 0, "exception");

		assertTrue(exception instanceof Exception);
	}

	@Test
	public void testLoggErrorsWhenMessageCouldBeRead() {
		RuntimeException exception = new RuntimeException();
		archivePathBuilder.MRV.setAlwaysThrowException("buildPathToAResourceInArchive", exception);

		messageReceiver.receiveMessage(headers, "someMessage");

		logger.MCR.assertParameters("logErrorUsingMessageAndException", 0,
				"Error while converting with type: someType, id: someId, dataDivider: someDataDivider and mimeType: someMimeType.",
				exception);
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
