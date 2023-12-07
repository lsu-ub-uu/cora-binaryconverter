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
package se.uu.ub.cora.binaryconverter.messagereceiver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.spy.BinaryOperationFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterSpy;
import se.uu.ub.cora.binaryconverter.spy.PathBuilderSpy;
import se.uu.ub.cora.binaryconverter.spy.ResourceMetadataCreatorSpy;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertImageToThumbnailsTest {

	private static final String JPEG_MIME_TYPE = "image/jpeg";
	private static final String SOME_DATA_DIVIDER = "someDataDivider";
	private static final String SOME_TYPE = "someType";
	private static final String SOME_ID = "someId";
	private static final String SOME_MESSAGE = "someMessage";

	private AnalyzeAndConvertImageToThumbnails converter;
	private Map<String, String> some_headers = new HashMap<>();
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
	private PathBuilderSpy pathBuilder;
	private ResourceMetadataCreatorSpy resourceMetadataCreator;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		dataClient = new DataClientSpy();
		binaryOperationFactory = new BinaryOperationFactorySpy();
		resourceMetadataCreator = new ResourceMetadataCreatorSpy();
		setUpImageAnalyzerFactory();

		converter = new AnalyzeAndConvertImageToThumbnails(dataClient, binaryOperationFactory,
				pathBuilder, resourceMetadataCreator);

		setMessageHeaders();
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);
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

		pathBuilder = new PathBuilderSpy();
		pathBuilder.MRV.setSpecificReturnValuesSupplier("buildPathToAFileAndEnsureFolderExists",
				() -> "aPath-thumbnail", SOME_DATA_DIVIDER, SOME_TYPE, SOME_ID + "-thumbnail");
		pathBuilder.MRV.setSpecificReturnValuesSupplier("buildPathToAFileAndEnsureFolderExists",
				() -> "aPath-medium", SOME_DATA_DIVIDER, SOME_TYPE, SOME_ID + "-medium");
		pathBuilder.MRV.setSpecificReturnValuesSupplier("buildPathToAFileAndEnsureFolderExists",
				() -> "aPath-large", SOME_DATA_DIVIDER, SOME_TYPE, SOME_ID + "-large");
	}

	private void setMessageHeaders() {
		some_headers.put("dataDivider", SOME_DATA_DIVIDER);
		some_headers.put("type", SOME_TYPE);
		some_headers.put("id", SOME_ID);
	}

	@Test
	public void testImageAnalyzerFactoryInitialized() throws Exception {
		assertTrue(converter instanceof MessageReceiver);
		var factory = converter.onlyForTestGetBinaryOperationFactory();
		assertNotNull(factory);
	}

	@Test
	public void testCallFactoryWithCorrectPath() throws Exception {
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		String resourceMasterPath = (String) pathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		binaryOperationFactory.MCR.assertParameters("factorImageAnalyzer", 0, resourceMasterPath);
	}

	@Test
	public void testCallPathBuilderBuild() throws Exception {

		converter.receiveMessage(some_headers, SOME_MESSAGE);

		pathBuilder.MCR.assertParameters("buildPathToAResourceInArchive", 0, SOME_DATA_DIVIDER,
				SOME_TYPE, SOME_ID);
	}

	@Test
	public void testCallAnalyze() throws Exception {

		converter.receiveMessage(some_headers, SOME_MESSAGE);

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", 0);

		analyzer.MCR.assertParameters("analyze", 0);
	}

	@Test
	public void testUpdateRecordAfterAnalyzing() throws Exception {
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		dataClient.MCR.assertParameters("read", 0, SOME_TYPE, SOME_ID);

		ClientDataRecordGroupSpy binaryRecordGroup = assertUpdateRecordAfterAnalyze();

		dataClient.MCR.assertParameters("update", 0, SOME_TYPE, SOME_ID, binaryRecordGroup);

	}

	private ClientDataRecordGroupSpy assertUpdateRecordAfterAnalyze() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		binaryRecordGroup.MCR.assertParameters("getFirstGroupWithNameInData", 0, "resourceInfo");

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", 0);
		ImageData imageData = (ImageData) analyzer.MCR.getReturnValue("analyze", 0);

		resourceMetadataCreator.MCR.assertParameters("updateMasterGroupFromResourceInfo", 0,
				getResourceInfo(), imageData);

		return binaryRecordGroup;
	}

	private ClientDataRecordGroupSpy getBinaryRecordGroup() {
		ClientDataRecordSpy dataRecord = (ClientDataRecordSpy) dataClient.MCR.getReturnValue("read",
				0);
		dataRecord.MCR.assertParameters("getDataRecordGroup", 0);
		ClientDataRecordGroupSpy binaryRecordGroup = (ClientDataRecordGroupSpy) dataRecord.MCR
				.getReturnValue("getDataRecordGroup", 0);
		return binaryRecordGroup;
	}

	@Test
	public void testConvertAndAnalyzeAndUpdateAllRepresentations() throws Exception {
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		String resourceMasterPath = (String) pathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		binaryOperationFactory.MCR.assertNumberOfCallsToMethod("factorImageAnalyzer", 4);

		assertAnalyzeAndConvertToRepresentation("large", 600, resourceMasterPath, 0, 1, 0);
		assertAnalyzeAndConvertToRepresentation("medium", 300, "aPath-large", 1, 2, 1);
		assertAnalyzeAndConvertToRepresentation("thumbnail", 100, "aPath-large", 2, 3, 2);

		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 0, "large",
				getResourceInfo(), SOME_ID, imageDataLarge, JPEG_MIME_TYPE);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 1, "medium",
				getResourceInfo(), SOME_ID, imageDataMedium, JPEG_MIME_TYPE);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 2,
				"thumbnail", getResourceInfo(), SOME_ID, imageDataThumbnail, JPEG_MIME_TYPE);
	}

	private void assertAnalyzeAndConvertToRepresentation(String representation, int width,
			String inputPath, int fImageConverterCallNr, int fAnalyzerCallNr,
			int pathBuilderCallNr) {

		String pathToFileRepresentation = assertConvertToRepresentation(representation, width,
				inputPath, fImageConverterCallNr, pathBuilderCallNr);
		assertAnalyzeRepresentation(representation, fAnalyzerCallNr, pathToFileRepresentation);
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
		imageConverter.MCR.assertParameters("convertUsingWidth", 0, inputPath,
				pathToFileRepresentation, width);
	}

	private String assertPathBuilderBuildFileSystemFilePath(String representation,
			int pathBuilderCallNr) {
		pathBuilder.MCR.assertMethodWasCalled("buildPathToAFileAndEnsureFolderExists");
		pathBuilder.MCR.assertParameters("buildPathToAFileAndEnsureFolderExists", pathBuilderCallNr,
				SOME_DATA_DIVIDER, SOME_TYPE, SOME_ID + "-" + representation);
		String pathToFileRepresentation = (String) pathBuilder.MCR
				.getReturnValue("buildPathToAFileAndEnsureFolderExists", pathBuilderCallNr);
		return pathToFileRepresentation;
	}

	private void assertAnalyzeRepresentation(String representation, int fAnalyzerCallNr,
			String pathToFileRepresentation) {
		binaryOperationFactory.MCR.assertParameters("factorImageAnalyzer", fAnalyzerCallNr,
				pathToFileRepresentation);
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", fAnalyzerCallNr);
		imageAnalyzer.MCR.assertParameters("analyze", 0);
	}

	private ClientDataGroupSpy getResourceInfo() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		return (ClientDataGroupSpy) binaryRecordGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
	}

	@Test
	public void testOnlyForTestGet() throws Exception {
		assertEquals(converter.onlyForTestGetDataClient(), dataClient);
		assertEquals(converter.onlyForTestGetBinaryOperationFactory(), binaryOperationFactory);
		assertEquals(converter.onlyForTestGetBinaryOperationFactory(), binaryOperationFactory);
		assertEquals(converter.onlyForTestGetPathBuilder(), pathBuilder);
		assertEquals(converter.onlyForTestGetResourceMetadataCreator(), resourceMetadataCreator);
	}
}
