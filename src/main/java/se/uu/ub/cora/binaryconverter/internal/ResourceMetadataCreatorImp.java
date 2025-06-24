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
import se.uu.ub.cora.clientdata.ClientDataResourceLink;

public class ResourceMetadataCreatorImp implements ResourceMetadataCreator {

	@Override
	public void updateMasterGroup(ClientDataGroup masterGroup, ImageData imageData) {

		ClientDataAtomic height = ClientDataProvider.createAtomicUsingNameInDataAndValue("height",
				imageData.height());
		ClientDataAtomic width = ClientDataProvider.createAtomicUsingNameInDataAndValue("width",
				imageData.width());
		ClientDataAtomic resolution = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("resolution", imageData.resolution());

		masterGroup.addChild(height);
		masterGroup.addChild(width);
		masterGroup.addChild(resolution);
	}

	@Override
	public ClientDataGroup createMetadataForRepresentation(String representation, String recordId,
			ImageData imageData, String mimeTypenName) {
		ClientDataGroup thumbnailGroup = ClientDataProvider
				.createGroupUsingNameInData(representation);

		ClientDataAtomic id = ClientDataProvider.createAtomicUsingNameInDataAndValue("resourceId",
				recordId + "-" + representation);
		ClientDataResourceLink resourceLink = ClientDataProvider
				.createResourceLinkUsingNameInDataAndTypeAndIdAndMimeType(representation, "binary",
						recordId, mimeTypenName);
		ClientDataAtomic fileSize = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("fileSize", imageData.size());
		ClientDataAtomic mimeType = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("mimeType", mimeTypenName);
		ClientDataAtomic height = ClientDataProvider.createAtomicUsingNameInDataAndValue("height",
				imageData.height());
		ClientDataAtomic width = ClientDataProvider.createAtomicUsingNameInDataAndValue("width",
				imageData.width());

		thumbnailGroup.addChild(id);
		thumbnailGroup.addChild(resourceLink);
		thumbnailGroup.addChild(fileSize);
		thumbnailGroup.addChild(mimeType);
		thumbnailGroup.addChild(height);
		thumbnailGroup.addChild(width);

		return thumbnailGroup;
	}
}