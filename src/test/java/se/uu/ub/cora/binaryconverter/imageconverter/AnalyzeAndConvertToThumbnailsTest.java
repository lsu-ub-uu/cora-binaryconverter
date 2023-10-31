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
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertToThumbnailsTest {

	private static final String SOME_DATA_DIVIDER = "someDataDivider";
	private static final String SOME_TYPE = "someType";
	private static final String SOME_ID = "someId";
	private static final String SHA256_OF_ID = "d8c88703e3133e12b4f9df4ec1df465a86af0e3a"
			+ "10710fb18db1f55f9ed40622";
	private static final String SOME_MESSAGE = "someMessage";
	private static final String SOME_OCFL_HOME = "/someOcflRootHome";

	private AnalyzeAndConvertToThumbnails imageSmallConverter;
	private Map<String, String> some_headers = new HashMap<>();
	private ImageAnalyzerFactorySpy imageAnalyzerFactory;
	private DataClientSpy dataClient;
	private ClientDataFactorySpy clientDataFactory;

	@BeforeMethod
	public void beforeMethod() {
		dataClient = new DataClientSpy();

		imageSmallConverter = new AnalyzeAndConvertToThumbnails(dataClient, SOME_OCFL_HOME);
		imageAnalyzerFactory = new ImageAnalyzerFactorySpy();

		setMessageHeaders();
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);
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
		assertEquals(imageSmallConverter.onlyForTestGetOcflHomePath(), SOME_OCFL_HOME);
	}

	@Test
	public void testOnlyForTestGetClientData() throws Exception {
		assertEquals(imageSmallConverter.onlyForTestGetDataClient(), dataClient);
	}

	@Test
	public void testCallFactoryWithCorrectPath() throws Exception {
		// echo -n "info:fedora/someDataDivider/resource/someType:someId-master" | sha256sum
		// d8c88703e3133e12b4f9df4ec1df465a86af0e3a10710fb18db1f55f9ed40622
		imageSmallConverter.onlyForTestSetImageAnalyzerFactory(imageAnalyzerFactory);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);
		imageAnalyzerFactory.MCR.assertParameters("factor", 0, SOME_OCFL_HOME + "/d8c/887/03e/"
				+ SHA256_OF_ID + "/v1/content/" + "someType:someId-master");
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
		ClientDataRecordSpy dataRecord = (ClientDataRecordSpy) dataClient.MCR.getReturnValue("read",
				0);
		ClientDataRecordGroupSpy binaryRecordGroup = assertUpdateRecordAfterAnalyze(dataRecord);

		dataClient.MCR.assertParameters("update", 0, SOME_TYPE, SOME_ID, binaryRecordGroup);

	}

	private ClientDataRecordGroupSpy assertUpdateRecordAfterAnalyze(
			ClientDataRecordSpy dataRecord) {
		dataRecord.MCR.assertParameters("getDataRecordGroup", 0);
		ClientDataRecordGroupSpy binaryRecordGroup = (ClientDataRecordGroupSpy) dataRecord.MCR
				.getReturnValue("getDataRecordGroup", 0);
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

	private ClientDataGroupSpy getGroupMasterFromBinary(ClientDataRecordGroupSpy dataRecordGroup) {
		ClientDataGroupSpy groupResourceInfo = (ClientDataGroupSpy) dataRecordGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
		groupResourceInfo.MCR.assertParameters("getFirstGroupWithNameInData", 0, "master");
		ClientDataGroupSpy groupMaster = (ClientDataGroupSpy) groupResourceInfo.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
		return groupMaster;
	}

}
