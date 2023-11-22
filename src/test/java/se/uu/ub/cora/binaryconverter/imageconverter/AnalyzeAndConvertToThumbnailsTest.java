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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy.ImageAnalyzerFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterSpy;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertToThumbnailsTest {

	private static final String IMAGE_JPEG = "image/jpeg";
	private static final String FILE_SYSTEM_BASE_PATH = "/someOutputPath/";
	private static final String ARCHIVE_BASE_PATH = "/someOcflRootHome";
	private static final String SOME_DATA_DIVIDER = "someDataDivider";
	private static final String SOME_TYPE = "someType";
	private static final String SOME_ID = "someId";
	private static final String SHA256_OF_ID = "d8c88703e3133e12b4f9df4ec1df465a86af0e3a"
			+ "10710fb18db1f55f9ed40622";
	private static final String SOME_MESSAGE = "someMessage";

	private static final String ARCHIVE_BASE_MASTER = ARCHIVE_BASE_PATH + "/d8c/887/03e/"
			+ SHA256_OF_ID + "/v1/content/" + "someType:someId-master";
	private static final String FILE_SYSTEM_PATH_FOR_RESOURCE = FILE_SYSTEM_BASE_PATH + "streams/"
			+ SOME_DATA_DIVIDER + "/" + SOME_ID;

	private AnalyzeAndConvertToThumbnails imageSmallConverter;
	private Map<String, String> some_headers = new HashMap<>();
	private ImageAnalyzerFactorySpy imageAnalyzerFactory;
	private DataClientSpy dataClient;
	private ClientDataFactorySpy clientDataFactory;
	private ImageConverterFactorySpy imageConverterFactory;
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

	@BeforeMethod
	public void beforeMethod() {
		setUpImageAnalyzerFactory();

		dataClient = new DataClientSpy();

		imageConverterFactory = new ImageConverterFactorySpy();

		imageSmallConverter = new AnalyzeAndConvertToThumbnails(dataClient, ARCHIVE_BASE_PATH,
				FILE_SYSTEM_BASE_PATH, imageConverterFactory);

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

		imageAnalyzerFactory = new ImageAnalyzerFactorySpy();

		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerMaster,
				ARCHIVE_BASE_MASTER);
		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerThumbnail,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-thumbnail");
		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerMedium,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-medium");
		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerLarge,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-large");
	}

	private void setMessageHeaders() {
		some_headers.put("dataDivider", SOME_DATA_DIVIDER);
		some_headers.put("type", SOME_TYPE);
		some_headers.put("id", SOME_ID);
	}

	@Test
	public void testImageAnalyzerFactoryInitialized() throws Exception {
		assertTrue(imageSmallConverter instanceof MessageReceiver);
		ImageAnalyzerFactory factory = imageSmallConverter.onlyForTestGetImageAnalyzerFactory();
		assertNotNull(factory);
	}

	@Test
	public void testOnlyForTestGetOcflHomePath() throws Exception {
		assertEquals(imageSmallConverter.onlyForTestGetOcflHomePath(), ARCHIVE_BASE_PATH);
	}

	@Test
	public void testOnlyForTestGetClientData() throws Exception {
		assertEquals(imageSmallConverter.onlyForTestGetDataClient(), dataClient);
	}

	@Test
	public void testCallFactoryWithCorrectPath() throws Exception {
		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);
		imageAnalyzerFactory.MCR.assertParameters("factor", 0, ARCHIVE_BASE_MASTER);
	}

	@Test
	public void testWrongAlgorithm() throws Exception {

		imageSmallConverter.onlyForTestSetHashAlgorithm("NonExistingAlgorithm");
		try {
			imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);
			fail("It should fail");
		} catch (Exception e) {
			assertTrue(e instanceof ImageConverterException);
			assertEquals(e.getMessage(), "Error while analyzing image.");
			assertEquals(e.getCause().getMessage(),
					"NonExistingAlgorithm MessageDigest not available");
		}
	}

	@Test
	public void testCallAnlayze() throws Exception {

		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) imageAnalyzerFactory.MCR
				.getReturnValue("factor", 0);

		analyzer.MCR.assertParameters("analyze", 0);
	}

	@Test
	public void testUpdateRecordAfterAnalyzing() throws Exception {
		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);

		dataClient.MCR.assertParameters("read", 0, SOME_TYPE, SOME_ID);

		ClientDataRecordGroupSpy binaryRecordGroup = assertUpdateRecordAfterAnalyze();

		dataClient.MCR.assertParameters("update", 0, SOME_TYPE, SOME_ID, binaryRecordGroup);

	}

	private ClientDataRecordGroupSpy assertUpdateRecordAfterAnalyze() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		binaryRecordGroup.MCR.assertParameters("getFirstGroupWithNameInData", 0, "resourceInfo");

		ClientDataGroupSpy groupMaster = getGroupMasterFromBinary(binaryRecordGroup);

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) imageAnalyzerFactory.MCR
				.getReturnValue("factor", 0);
		ImageData imageData = (ImageData) analyzer.MCR.getReturnValue("analyze", 0);

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 0, "height",
				imageData.height());
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 1, "width",
				imageData.width());
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 2,
				"resolution", imageData.resolution());

		var atomicHeight = clientDataFactory.MCR
				.getReturnValue("factorAtomicUsingNameInDataAndValue", 0);
		var atomicWidth = clientDataFactory.MCR
				.getReturnValue("factorAtomicUsingNameInDataAndValue", 1);
		var atomicResolution = clientDataFactory.MCR
				.getReturnValue("factorAtomicUsingNameInDataAndValue", 2);

		groupMaster.MCR.assertParameters("addChild", 0, atomicHeight);
		groupMaster.MCR.assertParameters("addChild", 1, atomicWidth);
		groupMaster.MCR.assertParameters("addChild", 2, atomicResolution);

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

	private ClientDataGroupSpy getGroupMasterFromBinary(ClientDataRecordGroupSpy dataRecordGroup) {
		ClientDataGroupSpy groupResourceInfo = (ClientDataGroupSpy) dataRecordGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
		groupResourceInfo.MCR.assertParameters("getFirstGroupWithNameInData", 0, "master");
		ClientDataGroupSpy groupMaster = (ClientDataGroupSpy) groupResourceInfo.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
		return groupMaster;
	}

	@Test
	public void testConvertAndAnalyzeSmallThumbnail() throws Exception {
		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);

		imageConverterFactory.MCR.assertParameters("factor", 0);
		ImageConverterSpy imageConverter = (ImageConverterSpy) imageConverterFactory.MCR
				.getReturnValue("factor", 0);
		imageConverter.MCR.assertParameters("convertUsingWidth", 0, ARCHIVE_BASE_MASTER,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-thumbnail", 100);

		imageAnalyzerFactory.MCR.assertParameters("factor", 1,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "" + "-thumbnail");
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) imageAnalyzerFactory.MCR
				.getReturnValue("factor", 1);
		imageAnalyzer.MCR.assertParameters("analyze", 0);
	}

	@Test
	public void testUpdateSmallThumbnailMetadata() throws Exception {
		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);

		clientDataFactory.MCR.assertParameters("factorGroupUsingNameInData", 0, "thumbnail");

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 3,
				"resourceId", SOME_ID + "-thumbnail");
		clientDataFactory.MCR.assertParameters("factorResourceLinkUsingNameInDataAndMimeType", 0,
				"thumbnail", IMAGE_JPEG);
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 4, "fileSize",
				imageDataThumbnail.size());
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 5, "mimeType",
				IMAGE_JPEG);
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 6, "height",
				imageDataThumbnail.height());
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 7, "width",
				imageDataThumbnail.width());
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 8,
				"resolution", imageDataThumbnail.resolution());

		ClientDataGroupSpy group = (ClientDataGroupSpy) clientDataFactory.MCR
				.getReturnValue("factorGroupUsingNameInData", 0);

		var resourceId = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				3);
		group.MCR.assertParameters("addChild", 0, resourceId);

		var resourceLink = clientDataFactory.MCR
				.getReturnValue("factorResourceLinkUsingNameInDataAndMimeType", 0);
		group.MCR.assertParameters("addChild", 1, resourceLink);

		var fileSize = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				4);
		group.MCR.assertParameters("addChild", 2, fileSize);

		var mimeType = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				5);
		group.MCR.assertParameters("addChild", 3, mimeType);

		var height = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue", 6);
		group.MCR.assertParameters("addChild", 4, height);

		var width = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue", 7);
		group.MCR.assertParameters("addChild", 5, width);

		var resolution = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				8);
		group.MCR.assertParameters("addChild", 6, resolution);

		ClientDataGroupSpy resourceInfo = getResourceInfo();
		resourceInfo.MCR.assertParameters("addChild", 0, group);

	}

	private ClientDataGroupSpy getResourceInfo() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		return (ClientDataGroupSpy) binaryRecordGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
	}
}
