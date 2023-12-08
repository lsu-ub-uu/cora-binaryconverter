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
package se.uu.ub.cora.binaryconverter.internal;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;

public class ResourceMetadataCreatorTest {

	private static final String SOME_RECORD_ID = "someRecordId";
	private static final String IMAGE_JPEG = "image/jpeg";

	private ResourceMetadataCreator resourceMetadataCreator;
	private ClientDataFactorySpy clientDataFactory;
	private ImageData imageData;
	private ClientDataGroupSpy masterGroup;

	@BeforeMethod
	private void beforeMethod() {
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);
		imageData = new ImageData("someResolution", "someWidth", "someHeight", "someSize");
		masterGroup = new ClientDataGroupSpy();

		resourceMetadataCreator = new ResourceMetadataCreatorImp();
	}

	@Test
	public void testInit() throws Exception {
		assertTrue(resourceMetadataCreator instanceof ResourceMetadataCreator);
	}

	@Test
	public void testCallCreateMetadataForRepresentation() throws Exception {

		ClientDataGroup representationDataGroup = resourceMetadataCreator
				.createMetadataForRepresentation("someRepresentation", SOME_RECORD_ID, imageData,
						IMAGE_JPEG);

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
				"resourceId", SOME_RECORD_ID + "-" + representationName);
		var resourceId = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				fAtomicCallNr);
		fAtomicCallNr++;

		clientDataFactory.MCR.assertParameters("factorResourceLinkUsingNameInDataAndMimeType",
				representationCallNr, representationName, IMAGE_JPEG);
		var resourceLink = clientDataFactory.MCR.getReturnValue(
				"factorResourceLinkUsingNameInDataAndMimeType", representationCallNr);

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
		fAtomicCallNr++;

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
	public void testCallCreateMasterGroupFromResourceInfo() throws Exception {

		resourceMetadataCreator.updateMasterGroup(masterGroup, imageData);
		assertUpdateRecordAfterAnalyze();
	}

	private void assertUpdateRecordAfterAnalyze() {

		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 0, "height",
				imageData.height());
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 1, "width",
				imageData.width());
		clientDataFactory.MCR.assertParameters("factorAtomicUsingNameInDataAndValue", 2,
				"resolution", imageData.resolution());

		var height = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue", 0);
		var width = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue", 1);
		var resolution = clientDataFactory.MCR.getReturnValue("factorAtomicUsingNameInDataAndValue",
				2);

		masterGroup.MCR.assertParameters("addChild", 0, height);
		masterGroup.MCR.assertParameters("addChild", 1, width);
		masterGroup.MCR.assertParameters("addChild", 2, resolution);

	}
}