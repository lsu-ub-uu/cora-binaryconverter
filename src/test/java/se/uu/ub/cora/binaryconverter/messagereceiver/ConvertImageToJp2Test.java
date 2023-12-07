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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.spy.BinaryOperationFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.binaryconverter.spy.Jp2ConverterSpy;
import se.uu.ub.cora.binaryconverter.spy.PathBuilderSpy;
import se.uu.ub.cora.binaryconverter.spy.ResourceMetadataCreatorSpy;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;

public class ConvertImageToJp2Test {

	private static final String JP2_MIME_TYPE = "image/jp2";
	private static final String SOME_DATA_DIVIDER = "someDataDivider";
	private static final String SOME_TYPE = "someType";
	private static final String SOME_ID = "someId";
	private static final String SOME_MESSAGE = "someMessage";

	private Map<String, String> some_headers = new HashMap<>();

	private ClientDataFactorySpy clientDataFactory;
	private ConvertImageToJp2 messageReceiver;
	private DataClientSpy dataClient;
	private BinaryOperationFactorySpy binaryOperationFactory;
	private PathBuilderSpy pathBuilder;

	private ResourceMetadataCreatorSpy resourceMetadataCreator;

	@BeforeMethod
	public void beforeMethod() {
		dataClient = new DataClientSpy();
		binaryOperationFactory = new BinaryOperationFactorySpy();

		pathBuilder = new PathBuilderSpy();

		resourceMetadataCreator = new ResourceMetadataCreatorSpy();

		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);

		messageReceiver = new ConvertImageToJp2(binaryOperationFactory, dataClient,
				resourceMetadataCreator, pathBuilder);

		setMessageHeaders();
	}

	private void setMessageHeaders() {
		some_headers.put("dataDivider", SOME_DATA_DIVIDER);
		some_headers.put("type", SOME_TYPE);
		some_headers.put("id", SOME_ID);
	}

	@Test
	public void testConvertImageToJp2Called() throws Exception {
		messageReceiver.receiveMessage(some_headers, SOME_MESSAGE);

		String resourceMasterPath = (String) pathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		assertAnalyzeAndConvertToRepresentation("jp2", 600, resourceMasterPath, 0);
	}

	@Test
	public void testConvertAndAnalyzeAndUpdateAllRepresentations() throws Exception {
		messageReceiver.receiveMessage(some_headers, SOME_MESSAGE);

		binaryOperationFactory.MCR.assertNumberOfCallsToMethod("factorImageAnalyzer", 1);
		var imageDataLarge = getImageData(0);

		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 0, "jp2",
				getResourceInfo(), SOME_ID, imageDataLarge, JP2_MIME_TYPE);

	}

	private ImageData getImageData(int callNr) {
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", callNr);
		return (ImageData) imageAnalyzer.MCR.getReturnValue("analyze", 0);
	}

	private void assertAnalyzeAndConvertToRepresentation(String representation, int width,
			String inputPath, int callNr) {

		String pathToFileRepresentation = assertConvertToRepresentation(representation, width,
				inputPath, callNr);
		assertAnalyzeRepresentation(representation, callNr, pathToFileRepresentation);
	}

	private String assertConvertToRepresentation(String representation, int width, String inputPath,
			int callNr) {

		String pathToFileRepresentation = assertPathBuilderBuildFileSystemFilePath(representation,
				callNr);
		assertCallToConvert(width, inputPath, callNr, pathToFileRepresentation);
		return pathToFileRepresentation;
	}

	private void assertCallToConvert(int width, String inputPath, int callNr,
			String pathToFileRepresentation) {
		binaryOperationFactory.MCR.assertParameters("factorJp2Converter", callNr);
		Jp2ConverterSpy jp2Converter = (Jp2ConverterSpy) binaryOperationFactory.MCR
				.getReturnValue("factorJp2Converter", callNr);
		jp2Converter.MCR.assertParameters("convert", 0, inputPath, "somePathToAFile");
	}

	private String assertPathBuilderBuildFileSystemFilePath(String representation, int callNr) {
		pathBuilder.MCR.assertMethodWasCalled("buildPathToAFileAndEnsureFolderExists");
		pathBuilder.MCR.assertParameters("buildPathToAFileAndEnsureFolderExists", callNr,
				SOME_DATA_DIVIDER, SOME_TYPE, SOME_ID + "-" + representation);
		String pathToFileRepresentation = (String) pathBuilder.MCR
				.getReturnValue("buildPathToAFileAndEnsureFolderExists", callNr);
		return pathToFileRepresentation;
	}

	private void assertAnalyzeRepresentation(String representation, int callNr,
			String pathToFileRepresentation) {
		binaryOperationFactory.MCR.assertParameters("factorImageAnalyzer", callNr,
				pathToFileRepresentation);
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) binaryOperationFactory.MCR
				.getReturnValue("factorImageAnalyzer", callNr);
		imageAnalyzer.MCR.assertParameters("analyze", 0);
	}

	private ClientDataGroupSpy getResourceInfo() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		return (ClientDataGroupSpy) binaryRecordGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
	}

	@Test
	public void testUpdateRecord() throws Exception {
		messageReceiver.receiveMessage(some_headers, SOME_MESSAGE);

		dataClient.MCR.assertParameters("read", 0, SOME_TYPE, SOME_ID);

		ClientDataRecordGroupSpy binaryRecordGroup = assertUpdateRecordAfterAnalyze();

		dataClient.MCR.assertParameters("update", 0, SOME_TYPE, SOME_ID, binaryRecordGroup);

	}

	private ClientDataRecordGroupSpy assertUpdateRecordAfterAnalyze() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		binaryRecordGroup.MCR.assertParameters("getFirstGroupWithNameInData", 0, "resourceInfo");

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
	public void testUpdateReturn_Conflict_409() throws Exception {

		dataClient.MRV.setReturnValues("update", List.of(new RuntimeException()));

	}

	@Test
	public void testOnlyForTestGet() throws Exception {
		assertEquals(messageReceiver.onlyForTestGetDataClient(), dataClient);
		assertEquals(messageReceiver.onlyForTestGetBinaryOperationFactory(),
				binaryOperationFactory);
		assertEquals(messageReceiver.onlyForTestGetPathBuilder(), pathBuilder);
		assertEquals(messageReceiver.onlyForTestGetResourceMetadataCreator(),
				resourceMetadataCreator);
	}

}
