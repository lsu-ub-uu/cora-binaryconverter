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
import se.uu.ub.cora.binaryconverter.spy.PdfConverterSpy;
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

public class ConvertPdfToThumbnailsTest {
	private static final String JPEG_MIME_TYPE = "image/jpeg";
	private static final String DATA_DIVIDER = "someDataDivider";
	private static final String TYPE = "someType";
	private static final String ID = "someId";
	private static final String MESSAGE = "someMessage";
	private LoggerFactorySpy loggerFactorySpy;

	private Map<String, String> headers = new HashMap<>();
	private ClientDataFactorySpy clientDataFactory;
	private DataClientSpy dataClient;
	private BinaryOperationFactorySpy binaryOperationFactory;
	private ArchivePathBuilderSpy archivePathBuilder;
	private ResourceMetadataCreatorSpy resourceMetadataCreator;

	private ConvertPdfToThumbnails messageReceiver;
	private StreamPathBuilderSpy streamPathBuilder;
	private LoggerSpy logger;
	private ClientDataRecordSpy recordFromStorage;
	private ClientDataRecordGroupSpy binaryRecordGroup;
	private ClientDataGroupSpy recordInfo;

	int supplierCount = 0;

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

		streamPathBuilder.MRV.setSpecificReturnValuesSupplier(
				"buildPathToAFileAndEnsureFolderExists", () -> "pathToFileLarge", DATA_DIVIDER,
				TYPE, ID, "large");
		streamPathBuilder.MRV.setSpecificReturnValuesSupplier(
				"buildPathToAFileAndEnsureFolderExists", () -> "pathToFileMedium", DATA_DIVIDER,
				TYPE, ID, "medium");
		streamPathBuilder.MRV.setSpecificReturnValuesSupplier(
				"buildPathToAFileAndEnsureFolderExists", () -> "pathToFileThumbnail", DATA_DIVIDER,
				TYPE, ID, "thumbnail");

		resourceMetadataCreator = new ResourceMetadataCreatorSpy();

		setUpDataClient();

		messageReceiver = new ConvertPdfToThumbnails(binaryOperationFactory, dataClient,
				resourceMetadataCreator, archivePathBuilder, streamPathBuilder);

		setMessageHeaders();
	}

	private void setMessageHeaders() {
		headers.put("dataDivider", DATA_DIVIDER);
		headers.put("type", TYPE);
		headers.put("id", ID);
	}

	private void setUpDataClient() {
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);

		recordFromStorage = new ClientDataRecordSpy();
		binaryRecordGroup = new ClientDataRecordGroupSpy();
		recordInfo = new ClientDataGroupSpy();

		recordFromStorage.MRV.setDefaultReturnValuesSupplier("getDataRecordGroup",
				() -> binaryRecordGroup);
		binaryRecordGroup.MRV.setSpecificReturnValuesSupplier("getFirstChildOfTypeAndName",
				() -> recordInfo, ClientDataGroup.class, "recordInfo");
		dataClient.MRV.setSpecificReturnValuesSupplier("read", () -> recordFromStorage, TYPE, ID);
	}

	@AfterMethod
	private void afterMethod() {
		supplierCount = 0;
	}

	@Test
	public void testLoggerStarted() {
		loggerFactorySpy.MCR.assertParameters("factorForClass", 0, ConvertPdfToThumbnails.class);
	}

	@Test
	public void testConvertPDfToThumbnailCalled() {
		messageReceiver.receiveMessage(headers, MESSAGE);

		String resourceMasterPath = (String) archivePathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		assertAnalyzeAndConvertToRepresentation("large", 1200, resourceMasterPath, 0, 0);

		String largePath = (String) streamPathBuilder.MCR.assertCalledParametersReturn(
				"buildPathToAFileAndEnsureFolderExists", DATA_DIVIDER, TYPE, ID, "large");

		assertAnalyzeAndConvertToRepresentation("medium", 600, largePath, 1, 2);
		assertAnalyzeAndConvertToRepresentation("thumbnail", 200, largePath, 2, 3);
	}

	@Test
	public void testConvertAndAnalyzeAndUpdateAllRepresentations() {
		messageReceiver.receiveMessage(headers, MESSAGE);

		binaryOperationFactory.MCR.assertNumberOfCallsToMethod("factorImageAnalyzer", 3);
		var imageDataLarge = getImageData(0);
		var imageDataMedium = getImageData(1);
		var imageDataThumbnail = getImageData(2);

		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 0,
				"pathToFileLarge", ID, "large", JPEG_MIME_TYPE, imageDataLarge);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 1,
				"pathToFileMedium", ID, "medium", JPEG_MIME_TYPE, imageDataMedium);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 2,
				"pathToFileThumbnail", ID, "thumbnail", JPEG_MIME_TYPE, imageDataThumbnail);

		var largeG = resourceMetadataCreator.MCR.getReturnValue("createMetadataForRepresentation",
				0);
		var mediumG = resourceMetadataCreator.MCR.getReturnValue("createMetadataForRepresentation",
				1);
		var thumbnailG = resourceMetadataCreator.MCR
				.getReturnValue("createMetadataForRepresentation", 2);

		binaryRecordGroup.MCR.assertParameters("addChild", 0, largeG);
		binaryRecordGroup.MCR.assertParameters("addChild", 1, mediumG);
		binaryRecordGroup.MCR.assertParameters("addChild", 2, thumbnailG);

		assertSetStatus("done");
	}

	private void assertSetStatus(String status) {
		recordInfo.MCR.assertCalledParameters("removeFirstChildWithTypeAndName",
				ClientDataAtomic.class, "status");
		ClientDataAtomicSpy statusAtomic = (ClientDataAtomicSpy) clientDataFactory.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "status",
						status);
		recordInfo.MCR.assertCalledParameters("addChild", statusAtomic);
	}

	private ImageData getImageData(int callNr) {
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", callNr);
		return (ImageData) imageAnalyzer.MCR.getReturnValue("analyze", 0);
	}

	private void assertAnalyzeAndConvertToRepresentation(String representation, int width,
			String inputPath, int callNr, int pathBuilderCallNr) {
		String pathToFileRepresentation = assertConvertToRepresentation(representation, width,
				inputPath, callNr, pathBuilderCallNr);
		assertAnalyzeRepresentation(callNr, pathToFileRepresentation);
	}

	private String assertConvertToRepresentation(String representation, int width, String inputPath,
			int callNr, int pathBuilderCallNr) {
		String pathToFileRepresentation = assertStreamPathBuilderBuildFileSystemFilePath(
				representation, pathBuilderCallNr);
		assertCallToConvert(width, inputPath, callNr, pathToFileRepresentation);
		return pathToFileRepresentation;
	}

	private void assertCallToConvert(int width, String inputPath, int callNr,
			String pathToFileRepresentation) {
		binaryOperationFactory.MCR.assertParameters("factorPdfConverter", callNr);
		PdfConverterSpy pdfConverter = (PdfConverterSpy) binaryOperationFactory.MCR
				.getReturnValue("factorPdfConverter", callNr);
		pdfConverter.MCR.assertParameters("convertUsingWidth", 0, inputPath,
				pathToFileRepresentation, width);
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

	private Object throwConflictExceptionOnFirstCall(DataClientException conflictException) {
		supplierCount++;
		if (supplierCount == 1) {
			throw conflictException;
		}
		return new ClientDataRecordSpy();
	}

	@Test
	public void testUpdateReturn_AnyOtherExceptionWithoutResponseCode() {
		DataClientException someException = DataClientException.withMessage("someError");
		dataClient.MRV.setAlwaysThrowException("update", someException);

		messageReceiver.receiveMessage(headers, MESSAGE);

		logger.MCR.assertNumberOfCallsToMethod("logErrorUsingMessageAndException", 2);
		assertLoggedError(0,
				"Error while converting with type: someType, id: someId and dataDivider: someDataDivider.",
				RuntimeException.class);
		assertLoggedError(1, "Error while converting.", BinaryConverterException.class);
		assertLatestTrhow(someException);

	}

	private void assertLatestTrhow(DataClientException expectedException) {
		Exception e = (Exception) logger.MCR.getParameterForMethodAndCallNumberAndParameter(
				"logErrorUsingMessageAndException", 1, "exception");
		assertEquals(e.getMessage(),
				"Binary record with id: " + ID + " could not be updated with conversion data.");
		assertEquals(e.getCause(), expectedException);
	}

	@Test
	public void testUpdateReturn_AnyOtherException() {
		DataClientException conflictException = DataClientException
				.withMessageAndResponseCode("someConflictError", 401);
		dataClient.MRV.setAlwaysThrowException("update", conflictException);

		messageReceiver.receiveMessage(headers, MESSAGE);

		logger.MCR.assertNumberOfCallsToMethod("logErrorUsingMessageAndException", 2);
		assertLoggedError(0,
				"Error while converting with type: someType, id: someId and dataDivider: someDataDivider.",
				RuntimeException.class);
		assertLoggedError(1, "Error while converting.", BinaryConverterException.class);
		assertLatestTrhow(conflictException);
	}

	@Test
	public void testLoggErrorIfMessageCouldNotBeRead() {
		messageReceiver.receiveMessage(null, null);

		logger.MCR.assertParameter("logErrorUsingMessageAndException", 0, "message",
				"Error while converting.");

		var exception = logger.MCR.getParameterForMethodAndCallNumberAndParameter(
				"logErrorUsingMessageAndException", 0, "exception");

		assertTrue(exception instanceof Exception);
	}

	@Test
	public void testLoggErrorsWhenErrorOccursOnConvertion() {
		RuntimeException exception = new RuntimeException();
		archivePathBuilder.MRV.setAlwaysThrowException("buildPathToAResourceInArchive", exception);

		messageReceiver.receiveMessage(headers, MESSAGE);

		logger.MCR.assertParameters("logErrorUsingMessageAndException", 0,
				"Error while converting with type: someType, id: someId and dataDivider: someDataDivider.",
				exception);

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
	public void testLoggErrorsWhenErrorOccursOnConvertionRetryUpdateIfConflict2() {
		RuntimeException exception = new RuntimeException();
		archivePathBuilder.MRV.setAlwaysThrowException("buildPathToAResourceInArchive", exception);

		DataClientException someException = DataClientException.withMessage("someError");
		dataClient.MRV.setAlwaysThrowException("update", someException);

		messageReceiver.receiveMessage(headers, MESSAGE);

		logger.MCR.assertNumberOfCallsToMethod("logErrorUsingMessageAndException", 2);
		assertLoggedError(0,
				"Error while converting with type: someType, id: someId and dataDivider: someDataDivider.",
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
