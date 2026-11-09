/*
 * Copyright 2023, 2025 Uppsala University Library
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
package se.uu.ub.cora.binaryconverter.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataAtomicSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;

public class ResourceMetadataCreatorTest {

	private static final String PATH_TO_STREAM = "/some/file/path";
	private static final String RECORD_ID = "someRecordId";
	private static final String IMAGE_JPEG = "image/jpeg";

	private ResourceMetadataCreator resourceMetadataCreator;
	private ClientDataFactorySpy clientDataFactory;
	private ImageData imageData;
	private ClientDataGroupSpy masterGroup;
	private ClientDataRecordGroupSpy recordGroup;
	private ClientDataGroupSpy recordInfo;

	@BeforeMethod
	private void beforeMethod() {
		setUpClientDataFactory();
		imageData = new ImageData("someResolution", "someWidth", "someHeight", "someSize");
		setUpRecordGroup();

		resourceMetadataCreator = new ResourceMetadataCreatorImp();
	}

	private void setUpClientDataFactory() {
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);
		createAtomic("resolution", "someResolution");
		createAtomic("width", "someWidth");
		createAtomic("height", "someHeight");
	}

	private void createAtomic(String nameInData, String value) {
		ClientDataAtomicSpy atomicSpy = new ClientDataAtomicSpy();
		atomicSpy.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> nameInData);
		clientDataFactory.MRV.setSpecificReturnValuesSupplier("factorAtomicUsingNameInDataAndValue",
				() -> atomicSpy, nameInData, value);
	}

	private void setUpRecordGroup() {
		masterGroup = new ClientDataGroupSpy();
		recordGroup = new ClientDataRecordGroupSpy();
		recordGroup.MRV.setSpecificReturnValuesSupplier("getFirstGroupWithNameInData",
				() -> masterGroup, "master");

		recordInfo = new ClientDataGroupSpy();

		recordGroup.MRV.setSpecificReturnValuesSupplier("getFirstChildOfTypeAndName",
				() -> recordInfo, ClientDataGroup.class, "recordInfo");
	}

	@Test
	public void testInit() {
		assertTrue(resourceMetadataCreator instanceof ResourceMetadataCreator);
	}

	@Test
	public void testCallCreateMetadataForRepresentation() {

		ClientDataGroup representationDataGroup = resourceMetadataCreator
				.createMetadataForRepresentation(PATH_TO_STREAM, RECORD_ID, "someRepresentation",
						IMAGE_JPEG, imageData);

		assertCreateAndUpdateMetadataForRespresentation("someRepresentation", imageData, 0, 0);
		clientDataFactory.MCR.assertReturn("factorGroupUsingNameInData", 0,
				representationDataGroup);
	}

	private void assertCreateAndUpdateMetadataForRespresentation(String representationName,
			ImageData imageData, int representationCallNr, int fAtomicCallNr) {

		clientDataFactory.MCR.assertParameters("factorGroupUsingNameInData", representationCallNr,
				representationName);
		ClientDataGroupSpy group = (ClientDataGroupSpy) clientDataFactory.MCR
				.getReturnValue("factorGroupUsingNameInData", representationCallNr);

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", fAtomicCallNr,
				"resourceId", PATH_TO_STREAM);
		var resourceId = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				fAtomicCallNr);
		fAtomicCallNr++;

		var resourceLink = clientDataFactory.MCR.assertCalledParametersReturn(
				"factorResourceLinkUsingNameInDataAndTypeAndIdAndMimeType", representationName,
				"binary", RECORD_ID, IMAGE_JPEG);

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", fAtomicCallNr,
				"fileSize", imageData.size());
		var fileSize = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				fAtomicCallNr);
		fAtomicCallNr++;

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", fAtomicCallNr,
				"mimeType", IMAGE_JPEG);
		var mimeType = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				fAtomicCallNr);
		fAtomicCallNr++;

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", fAtomicCallNr,
				"height", imageData.height());
		var height = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				fAtomicCallNr);
		fAtomicCallNr++;

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", fAtomicCallNr,
				"width", imageData.width());
		var width = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				fAtomicCallNr);

		clientDataFactory.MCR.assertNumberOfCallsToMethod("factorAtomicUsingNameInDataAndValue", 5);

		group.MCR.assertParameters("addChild", 0, resourceId);
		group.MCR.assertParameters("addChild", 1, resourceLink);
		group.MCR.assertParameters("addChild", 2, fileSize);
		group.MCR.assertParameters("addChild", 3, mimeType);
		group.MCR.assertParameters("addChild", 4, height);
		group.MCR.assertParameters("addChild", 5, width);

		group.MCR.assertNumberOfCallsToMethod("addChild", 6);
	}

	@Test
	public void testCallCreateMasterGroup() {
		resourceMetadataCreator.updateMasterGroup(recordGroup, imageData);
		assertUpdateRecordAfterAnalyze();
	}

	private void assertUpdateRecordAfterAnalyze() {
		ClientDataAtomic height = (ClientDataAtomic) clientDataFactory.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "height",
						imageData.height());
		ClientDataAtomic width = (ClientDataAtomic) clientDataFactory.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "width",
						imageData.width());
		ClientDataAtomic resolution = (ClientDataAtomic) clientDataFactory.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "resolution",
						imageData.resolution());

		ClientDataGroupSpy usedMasterG = (ClientDataGroupSpy) recordGroup.MCR
				.assertCalledParametersReturn("getFirstGroupWithNameInData", "master");
		usedMasterG.MCR.assertCalledParameters("removeAllChildrenWithNameInData",
				height.getNameInData());
		masterGroup.MCR.assertParameters("addChild", 0, height);
		masterGroup.MCR.assertParameters("addChild", 1, width);
		masterGroup.MCR.assertParameters("addChild", 2, resolution);
	}

	@Test
	public void testSetChildWithNameInData() {
		ClientDataGroupSpy childSpy = new ClientDataGroupSpy();
		childSpy.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "someNameInData");

		resourceMetadataCreator.setChildInRecordGroup(childSpy, recordGroup);

		recordGroup.MCR.assertCalledParameters("removeAllChildrenWithNameInData",
				childSpy.getNameInData());
		recordGroup.MCR.assertCalledParameters("addChild", childSpy);
	}

	@Test
	public void testGetStatus() {
		String status = resourceMetadataCreator.getStatusFromRecordGroup(recordGroup);

		var status2 = recordInfo.MCR
				.assertCalledParametersReturn("getFirstAtomicValueWithNameInData", "status");
		assertSame(status, status2);
	}

	@Test
	public void testSetStatus() {
		resourceMetadataCreator.setStatusInRecordGroup("someStatus", recordGroup);

		recordInfo.MCR.assertCalledParameters("removeAllChildrenWithNameInData", "status");
		var status = clientDataFactory.MCR.assertCalledParametersReturn(
				"factorAtomicUsingNameInDataAndValue", "status", "someStatus");
		recordInfo.MCR.assertCalledParameters("addChild", status);
	}

	@Test
	public void testAddProcessingMessageInRecordGroup() {
		resourceMetadataCreator.addProcessingMessageInRecordGroup("someMessage", recordGroup);

		String repeatId = (String) clientDataFactory.MCR
				.getParameterForMethodAndCallNumberAndParameter(
						"factorAtomicUsingNameInDataAndValueAndRepeatId", 0, "repeatId");
		assertEquals(repeatId.length(), 13);

		ClientDataAtomicSpy messageAtomic = (ClientDataAtomicSpy) clientDataFactory.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValueAndRepeatId",
						"processingMessage", "someMessage");
		recordInfo.MCR.assertCalledParameters("addChild", messageAtomic);
	}
}