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
import static org.testng.Assert.assertNotNull;
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
import se.uu.ub.cora.binaryconverter.spy.ImageConverterSpy;
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
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.storage.spies.path.ArchivePathBuilderSpy;
import se.uu.ub.cora.storage.spies.path.StreamPathBuilderSpy;

public class AnalyzeAndConvertImageToThumbnailsTest {

	private static final String JPEG_MIME_TYPE = "image/jpeg";
	private static final String DATA_DIVIDER = "someDataDivider";
	private static final String TYPE = "someType";
	private static final String ID = "someId";
	private static final String SOME_MESSAGE = "someMessage";

	private LoggerFactorySpy loggerFactorySpy;
	private AnalyzeAndConvertImageToThumbnails converter;
	private Map<String, String> headers = new HashMap<>();
	private BinaryOperationFactorySpy binaryOperationFactory;
	private DataClientSpy dataClient;
	private ClientDataFactorySpy clientDataFactory;
	private ImageAnalyzerSpy analyzerMaster;
	private ImageAnalyzerSpy analyzerThumbnail;
	private ImageAnalyzerSpy analyzerMedium;
	private ImageAnalyzerSpy analyzerLarge;
	private ImageData imageDataMaster = new ImageData("resMaster", "widthMaster", "heightMaster",
			"sizeMaster");
	private ImageData imageDataThumbnail = new ImageData("resThumbnail", "widthThumbnail",
			"heightThumbnail", "sizeSmall");
	private ImageData imageDataMedium = new ImageData("resMedium", "widthMedium", "heightMedium",
			"sizeMedium");
	private ImageData imageDataLarge = new ImageData("resLarge", "widthLarge", "heightLarge",
			"sizeLarge");
	private ArchivePathBuilderSpy archivePathBuilder;
	private StreamPathBuilderSpy streamPathBuilder;
	private ResourceMetadataCreatorSpy resourceMetadataCreator;
	private LoggerSpy logger;
	private ClientDataRecordSpy dataRecord;
	private ClientDataRecordGroupSpy binaryRecordGroup;
	private ClientDataGroupSpy recordInfo;

	@BeforeMethod
	public void beforeMethod() {
		logger = new LoggerSpy();
		loggerFactorySpy = new LoggerFactorySpy();
		loggerFactorySpy.MRV.setDefaultReturnValuesSupplier("factorForClass", () -> logger);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataClient = new DataClientSpy();
		binaryOperationFactory = new BinaryOperationFactorySpy();
		resourceMetadataCreator = new ResourceMetadataCreatorSpy();
		archivePathBuilder = new ArchivePathBuilderSpy();
		streamPathBuilder = new StreamPathBuilderSpy();
		setUpImageAnalyzerFactory();

		converter = new AnalyzeAndConvertImageToThumbnails(dataClient, binaryOperationFactory,
				archivePathBuilder, streamPathBuilder, resourceMetadataCreator);

		setMessageHeaders();
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);

		setReadRecordOnStorage();
	}

	@AfterMethod
	private void afterMethod() {
		supplierCount = 0;
	}

	private void setUpImageAnalyzerFactory() {
		analyzerMaster = new ImageAnalyzerSpy();
		analyzerThumbnail = new ImageAnalyzerSpy();
		analyzerMedium = new ImageAnalyzerSpy();
		analyzerLarge = new ImageAnalyzerSpy();

		analyzerMaster.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataMaster);
		analyzerThumbnail.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataThumbnail);
		analyzerMedium.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataMedium);
		analyzerLarge.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataLarge);

		binaryOperationFactory.MRV.setSpecificReturnValuesSupplier("factorImageAnalyzer",
				() -> analyzerMaster, "somePathToArchive");
		binaryOperationFactory.MRV.setSpecificReturnValuesSupplier("factorImageAnalyzer",
				() -> analyzerThumbnail, "aPath-thumbnail");
		binaryOperationFactory.MRV.setSpecificReturnValuesSupplier("factorImageAnalyzer",
				() -> analyzerMedium, "aPath-medium");
		binaryOperationFactory.MRV.setSpecificReturnValuesSupplier("factorImageAnalyzer",
				() -> analyzerLarge, "aPath-large");

		streamPathBuilder.MRV.setSpecificReturnValuesSupplier(
				"buildPathToAFileAndEnsureFolderExists", () -> "aPath-thumbnail", DATA_DIVIDER,
				TYPE, ID, "thumbnail");
		streamPathBuilder.MRV.setSpecificReturnValuesSupplier(
				"buildPathToAFileAndEnsureFolderExists", () -> "aPath-medium", DATA_DIVIDER, TYPE,
				ID, "medium");
		streamPathBuilder.MRV.setSpecificReturnValuesSupplier(
				"buildPathToAFileAndEnsureFolderExists", () -> "aPath-large", DATA_DIVIDER, TYPE,
				ID, "large");
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
	}

	@Test
	public void testLoggerStarted() {
		loggerFactorySpy.MCR.assertParameters("factorForClass", 0,
				AnalyzeAndConvertImageToThumbnails.class);
	}

	@Test
	public void testImageAnalyzerFactoryInitialized() {
		assertTrue(converter instanceof MessageReceiver);
		var factory = converter.onlyForTestGetBinaryOperationFactory();
		assertNotNull(factory);
	}

	@Test
	public void testCallFactoryWithCorrectPath() {
		converter.receiveMessage(headers, SOME_MESSAGE);

		String resourceMasterPath = (String) archivePathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		binaryOperationFactory.MCR.assertParameters("factorImageAnalyzer", 0, resourceMasterPath);
	}

	@Test
	public void testCallPathBuilderBuild() {
		converter.receiveMessage(headers, SOME_MESSAGE);

		archivePathBuilder.MCR.assertParameters("buildPathToAResourceInArchive", 0, DATA_DIVIDER,
				TYPE, ID);
	}

	@Test
	public void testCallAnalyze() {
		converter.receiveMessage(headers, SOME_MESSAGE);

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", 0);

		analyzer.MCR.assertParameters("analyze", 0);
	}

	@Test
	public void testUpdateRecordAfterAnalyzing() {
		converter.receiveMessage(headers, SOME_MESSAGE);

		dataClient.MCR.assertParameters("read", 0, TYPE, ID);

		ClientDataRecordGroupSpy binaryRecordGroup = assertUpdateRecordAfterAnalyze();

		dataClient.MCR.assertParameters("update", 0, TYPE, ID, binaryRecordGroup);
	}

	private ClientDataRecordGroupSpy assertUpdateRecordAfterAnalyze() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		binaryRecordGroup.MCR.assertParameters("getFirstGroupWithNameInData", 0, "master");
		var masterG = binaryRecordGroup.MCR.getReturnValue("getFirstGroupWithNameInData", 0);

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", 0);
		ImageData imageData = (ImageData) analyzer.MCR.getReturnValue("analyze", 0);

		resourceMetadataCreator.MCR.assertParameters("updateMasterGroup", 0, masterG, imageData);

		return binaryRecordGroup;
	}

	private ClientDataRecordGroupSpy getBinaryRecordGroup() {
		ClientDataRecordSpy dataRecord = (ClientDataRecordSpy) dataClient.MCR.getReturnValue("read",
				0);
		dataRecord.MCR.assertParameters("getDataRecordGroup", 0);
		return (ClientDataRecordGroupSpy) dataRecord.MCR.getReturnValue("getDataRecordGroup", 0);
	}

	@Test
	public void testConvertAndAnalyzeAndUpdateAllRepresentations() {
		converter.receiveMessage(headers, SOME_MESSAGE);

		String resourceMasterPath = (String) archivePathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		binaryOperationFactory.MCR.assertNumberOfCallsToMethod("factorImageAnalyzer", 4);

		assertAnalyzeAndConvertToRepresentation("large", 1200, resourceMasterPath, 0, 1, 0);

		String largePath = (String) streamPathBuilder.MCR.assertCalledParametersReturn(
				"buildPathToAFileAndEnsureFolderExists", DATA_DIVIDER, TYPE, ID, "large");

		assertAnalyzeAndConvertToRepresentation("medium", 600, largePath, 1, 2, 2);
		assertAnalyzeAndConvertToRepresentation("thumbnail", 200, largePath, 2, 3, 3);

		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 0,
				"aPath-large", ID, "large", JPEG_MIME_TYPE, imageDataLarge);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 1,
				"aPath-medium", ID, "medium", JPEG_MIME_TYPE, imageDataMedium);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 2,
				"aPath-thumbnail", ID, "thumbnail", JPEG_MIME_TYPE, imageDataThumbnail);

		var largeG = resourceMetadataCreator.MCR.getReturnValue("createMetadataForRepresentation",
				0);
		var mediumG = resourceMetadataCreator.MCR.getReturnValue("createMetadataForRepresentation",
				1);
		var thumbnailG = resourceMetadataCreator.MCR
				.getReturnValue("createMetadataForRepresentation", 2);

		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();

		binaryRecordGroup.MCR.assertParameters("addChild", 0, largeG);
		binaryRecordGroup.MCR.assertParameters("addChild", 1, mediumG);
		binaryRecordGroup.MCR.assertParameters("addChild", 2, thumbnailG);
	}

	private void assertAnalyzeAndConvertToRepresentation(String representation, int width,
			String inputPath, int fImageConverterCallNr, int fAnalyzerCallNr,
			int pathBuilderCallNr) {
		String pathToFileRepresentation = assertConvertToRepresentation(representation, width,
				inputPath, fImageConverterCallNr, pathBuilderCallNr);
		assertAnalyzeRepresentation(fAnalyzerCallNr, pathToFileRepresentation);
	}

	private String assertConvertToRepresentation(String representation, int width, String inputPath,
			int fImageConverterCallNr, int pathBuilderCallNr) {
		String pathToFileRepresentation = assertPathBuilderBuildFileSystemFilePath(representation,
				pathBuilderCallNr);
		assertCallToConvert(width, inputPath, fImageConverterCallNr, pathToFileRepresentation);
		return pathToFileRepresentation;
	}

	private void assertCallToConvert(int width, String inputPath, int fImageConverterCallNr,
			String pathToFileRepresentation) {
		binaryOperationFactory.MCR.assertParameters("factorImageConverter", fImageConverterCallNr);
		ImageConverterSpy imageConverter = (ImageConverterSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageConverter", fImageConverterCallNr);
		imageConverter.MCR.assertParameters("convertAndResizeUsingWidth", 0, inputPath,
				pathToFileRepresentation, width);
	}

	private String assertPathBuilderBuildFileSystemFilePath(String representation,
			int pathBuilderCallNr) {
		streamPathBuilder.MCR.assertMethodWasCalled("buildPathToAFileAndEnsureFolderExists");
		streamPathBuilder.MCR.assertParameters("buildPathToAFileAndEnsureFolderExists",
				pathBuilderCallNr, DATA_DIVIDER, TYPE, ID, representation);
		return (String) streamPathBuilder.MCR
				.getReturnValue("buildPathToAFileAndEnsureFolderExists", pathBuilderCallNr);
	}

	private void assertAnalyzeRepresentation(int fAnalyzerCallNr, String pathToFileRepresentation) {
		binaryOperationFactory.MCR.assertParameters("factorImageAnalyzer", fAnalyzerCallNr,
				pathToFileRepresentation);
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", fAnalyzerCallNr);
		imageAnalyzer.MCR.assertParameters("analyze", 0);
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

		converter.receiveMessage(headers, SOME_MESSAGE);

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
	public void testUpdateReturn_AnyOtherExceptionWithoutResponseCode() {
		DataClientException someException = DataClientException.withMessage("someError");

		dataClient.MRV.setAlwaysThrowException("update", someException);
		converter.receiveMessage(headers, SOME_MESSAGE);
		assertLogWhenException(someException);
		assertSetStatus("failed");
	}

	private void assertLogWhenException(DataClientException conflictException) {
		logger.MCR.assertNumberOfCallsToMethod("logErrorUsingMessageAndException", 2);

		logger.MCR.assertParameter("logErrorUsingMessageAndException", 0, "message",
				"Error while converting with type: someType, id: someId "
						+ "and dataDivider: someDataDivider.");

		RuntimeException loggedException = (RuntimeException) logger.MCR
				.getParameterForMethodAndCallNumberAndParameter("logErrorUsingMessageAndException",
						0, "exception");

		assertTrue(loggedException instanceof BinaryConverterException);
		assertEquals(loggedException.getMessage(),
				"Binary record with id: someId could not be updated with conversion data.");

		assertSame(loggedException.getCause(), conflictException);
	}

	@Test
	public void testUpdateReturn_AnyOtherException() {
		DataClientException someException = DataClientException
				.withMessageAndResponseCode("someError", 666);
		dataClient.MRV.setAlwaysThrowException("update", someException);

		converter.receiveMessage(headers, SOME_MESSAGE);

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

		converter.receiveMessage(headers, SOME_MESSAGE);

		dataClient.MCR.assertNumberOfCallsToMethod("update", 2);
	}

	@Test
	public void testLoggErrorIfMessageCouldNotBeRead() {
		converter.receiveMessage(null, null);

		logger.MCR.assertParameter("logErrorUsingMessageAndException", 0, "message",
				"Error while converting. Could not read message from queue.");

		var exception = logger.MCR.getParameterForMethodAndCallNumberAndParameter(
				"logErrorUsingMessageAndException", 0, "exception");

		assertTrue(exception instanceof Exception);
	}

	@Test
	public void testLoggErrorsWhenMessageCouldBeRead() {
		RuntimeException exception = new RuntimeException();
		archivePathBuilder.MRV.setAlwaysThrowException("buildPathToAResourceInArchive", exception);

		converter.receiveMessage(headers, SOME_MESSAGE);

		logger.MCR.assertParameters("logErrorUsingMessageAndException", 0,
				"Error while converting with type: someType, id: someId and dataDivider: someDataDivider.",
				exception);
	}

	@Test
	public void testSetStatusToProcessing() {
		setUpStatusOnReadRecordFromStorage("uploaded");

		converter.receiveMessage(headers, SOME_MESSAGE);

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

		converter.receiveMessage(headers, SOME_MESSAGE);

		recordInfo.MCR.assertParameters("getFirstAtomicValueWithNameInData", 0, "status");
		assertSetStatus("done");
	}

	@Test
	public void testOnlyForTestGet() {
		assertEquals(converter.onlyForTestGetDataClient(), dataClient);
		assertEquals(converter.onlyForTestGetBinaryOperationFactory(), binaryOperationFactory);
		assertEquals(converter.onlyForTestGetBinaryOperationFactory(), binaryOperationFactory);
		assertEquals(converter.onlyForTestGetArchivePathBuilder(), archivePathBuilder);
		assertEquals(converter.onlyForTestGetResourceMetadataCreator(), resourceMetadataCreator);
		assertEquals(converter.onlyForTestGetStreamPathBuilder(), streamPathBuilder);
	}

	@Test
	public void testTopicClosed() {
		converter.topicClosed();

		logger.MCR.assertParameters("logFatalUsingMessage", 0, "Topic is closed!");
	}
}
