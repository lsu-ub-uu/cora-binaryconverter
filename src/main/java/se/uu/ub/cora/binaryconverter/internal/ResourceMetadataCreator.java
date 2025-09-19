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

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataGroup;

public interface ResourceMetadataCreator {

	/**
	 * createMetadataForRepresentation method creates a new group using representation as nameInData
	 * and filling all the fields using imageData.
	 * 
	 * @param pathToStream
	 *            string with the path to the stream
	 * @param recordId
	 *            id of the record
	 * @param representation
	 *            name of the representation
	 * @param imageData
	 *            an image data {@link ImageData} of the representation
	 * @return a {@link ClientDataGroup}
	 */
	ClientDataGroup createMetadataForRepresentation(String pathToStream, String recordId,
			String representation, String mimeType, ImageData imageData);

	/**
	 * updateMasterGroup method updates masterGroup using imageData as input.
	 * @param masterGroup
	 *            is the master representation
	 * @param imageData
	 *            image data of the representation
	 */
	void updateMasterGroup(ClientDataGroup masterGroup, ImageData imageData);

}
