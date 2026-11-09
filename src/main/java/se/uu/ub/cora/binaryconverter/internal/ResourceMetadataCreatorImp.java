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

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public class ResourceMetadataCreatorImp implements ResourceMetadataCreator {

	private static final String RECORD_INFO = "recordInfo";
	private static final String STATUS = "status";
	private static final String RESOLUTION = "resolution";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";

	@Override
	public void updateMasterGroup(ClientDataRecordGroup recordGroup, ImageData imageData) {
		ClientDataGroup masterG = recordGroup.getFirstGroupWithNameInData("master");
		masterG.removeAllChildrenWithNameInData(HEIGHT);
		masterG.removeAllChildrenWithNameInData(WIDTH);
		masterG.removeAllChildrenWithNameInData(RESOLUTION);
		ClientDataAtomic height = ClientDataProvider.createAtomicUsingNameInDataAndValue(HEIGHT,
				imageData.height());
		ClientDataAtomic width = ClientDataProvider.createAtomicUsingNameInDataAndValue(WIDTH,
				imageData.width());
		ClientDataAtomic resolution = ClientDataProvider
				.createAtomicUsingNameInDataAndValue(RESOLUTION, imageData.resolution());

		masterG.addChild(height);
		masterG.addChild(width);
		masterG.addChild(resolution);
	}

	@Override
	public ClientDataGroup createMetadataForRepresentation(String pathToStream, String recordId,
			String representation, String mimeTypenIn, ImageData imageData) {
		ClientDataGroup thumbnailGroup = ClientDataProvider
				.createGroupUsingNameInData(representation);

		ClientDataAtomic id = ClientDataProvider.createAtomicUsingNameInDataAndValue("resourceId",
				pathToStream);
		ClientDataResourceLink resourceLink = ClientDataProvider
				.createResourceLinkUsingNameInDataAndTypeAndIdAndMimeType(representation, "binary",
						recordId, mimeTypenIn);
		ClientDataAtomic fileSize = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("fileSize", imageData.size());
		ClientDataAtomic mimeType = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("mimeType", mimeTypenIn);
		ClientDataAtomic height = ClientDataProvider.createAtomicUsingNameInDataAndValue(HEIGHT,
				imageData.height());
		ClientDataAtomic width = ClientDataProvider.createAtomicUsingNameInDataAndValue(WIDTH,
				imageData.width());

		thumbnailGroup.addChild(id);
		thumbnailGroup.addChild(resourceLink);
		thumbnailGroup.addChild(fileSize);
		thumbnailGroup.addChild(mimeType);
		thumbnailGroup.addChild(height);
		thumbnailGroup.addChild(width);

		return thumbnailGroup;
	}

	@Override
	public void setChildInRecordGroup(ClientDataGroup child, ClientDataRecordGroup recordGroup) {
		recordGroup.removeAllChildrenWithNameInData(child.getNameInData());
		recordGroup.addChild(child);
	}

	@Override
	public void setStatusInRecordGroup(String status, ClientDataRecordGroup recordGroup) {
		ClientDataGroup recordInfo = recordGroup.getFirstChildOfTypeAndName(ClientDataGroup.class,
				RECORD_INFO);
		recordInfo.removeAllChildrenWithNameInData(STATUS);
		ClientDataAtomic statusUploaded = ClientDataProvider
				.createAtomicUsingNameInDataAndValue(STATUS, status);
		recordInfo.addChild(statusUploaded);
	}

	@Override
	public String getStatusFromRecordGroup(ClientDataRecordGroup recordGroup) {
		ClientDataGroup recordInfo = recordGroup.getFirstChildOfTypeAndName(ClientDataGroup.class,
				RECORD_INFO);
		return recordInfo.getFirstAtomicValueWithNameInData(STATUS);
	}

	@Override
	public void addProcessingMessageInRecordGroup(String message,
			ClientDataRecordGroup recordGroup) {
		ClientDataGroup recordInfo = recordGroup.getFirstChildOfTypeAndName(ClientDataGroup.class,
				RECORD_INFO);
		ClientDataAtomic processingMessage = ClientDataProvider
				.createAtomicUsingNameInDataAndValueAndRepeatId("processingMessage", message,
						String.valueOf(System.currentTimeMillis()));
		recordInfo.addChild(processingMessage);
	}

}