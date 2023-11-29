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
package se.uu.ub.cora.binaryconverter.common;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public class ResourceMetadataCreatorImp implements ResourceMetadataCreator {
	private static final String IMAGE_JPEG = "image/jpeg";

	@Override
	public void createMetadataForRepresentation(String representation,
			ClientDataGroup resourceInfoGroup, String recordId, ImageData imageData) {
		ClientDataGroup thumbnailGroup = ClientDataProvider
				.createGroupUsingNameInData(representation);

		ClientDataAtomic id = ClientDataProvider.createAtomicUsingNameInDataAndValue("resourceId",
				recordId + "-" + representation);
		ClientDataResourceLink resourceLink = ClientDataProvider
				.createResourceLinkUsingNameInDataAndMimeType(representation, IMAGE_JPEG);
		ClientDataAtomic fileSize = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("fileSize", imageData.size());
		ClientDataAtomic mimeType = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("mimeType", IMAGE_JPEG);
		ClientDataAtomic height = ClientDataProvider.createAtomicUsingNameInDataAndValue("height",
				imageData.height());
		ClientDataAtomic width = ClientDataProvider.createAtomicUsingNameInDataAndValue("width",
				imageData.width());
		ClientDataAtomic resolution = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("resolution", imageData.resolution());

		thumbnailGroup.addChild(id);
		thumbnailGroup.addChild(resourceLink);
		thumbnailGroup.addChild(fileSize);
		thumbnailGroup.addChild(mimeType);
		thumbnailGroup.addChild(height);
		thumbnailGroup.addChild(width);
		thumbnailGroup.addChild(resolution);

		resourceInfoGroup.addChild(thumbnailGroup);
	}

	@Override
	public void updateMasterGroupFromResourceInfo(ClientDataGroup resourceInfoGroup,
			ImageData imageData) {

		ClientDataGroup masterGroup = resourceInfoGroup.getFirstGroupWithNameInData("master");

		ClientDataAtomic atomicHeight = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("height", imageData.height());
		ClientDataAtomic atomicWidth = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("width", imageData.width());
		ClientDataAtomic atomicResolution = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("resolution", imageData.resolution());

		masterGroup.addChild(atomicHeight);
		masterGroup.addChild(atomicWidth);
		masterGroup.addChild(atomicResolution);
	}
}
