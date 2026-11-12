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
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;

public interface ResourceMetadataCreator {

	/**
	 * updateMasterGroup method updates masterGroup in the provided recordGroup using imageData as
	 * input for height, width and resolution. If any of the these exist since before are they
	 * removed before the new data is added.
	 * 
	 * @param recordGroup
	 *            To update the master representation in
	 * @param imageData
	 *            image data of the representation
	 */
	void updateMasterGroup(ClientDataRecordGroup recordGroup, ImageData imageData);

	/**
	 * createMetadataForRepresentation method creates a new group using representation as nameInData
	 * and filling all the fields using imageData.
	 * 
	 * @param pathToStream
	 *            string with the path to the stream
	 * @param recordId
	 *            id of the record
	 * @param representation
	 *            name of the representation, possible values are thumbnail, medium, large, jp2
	 * @param imageData
	 *            an image data {@link ImageData} of the representation
	 * @return a {@link ClientDataGroup}
	 */
	ClientDataGroup createMetadataForRepresentation(String pathToStream, String recordId,
			String representation, String mimeType, ImageData imageData);

	/**
	 * setChildInRecordGroup adds a child to the provided recordGroup, using the childs nameInData.
	 * If the nameInData exists as a child in the recordGroup since before are all children with
	 * that name first removed, then is the provided child added.
	 * 
	 * @param child
	 *            The child to set
	 * @param recordGroup
	 *            To set the child in
	 */
	void setChildInRecordGroup(ClientDataGroup child, ClientDataRecordGroup recordGroup);

	/**
	 * setStatusInRecordGroup sets the status in recordInfo in the provided recordGroup, using
	 * nameInData status. If status existed since before is the child first removed from recordInfo
	 * 
	 * @param status
	 *            A String with the status to set
	 * @param recordGroup
	 *            To set the child in status in its recordInfo
	 */
	void setStatusInRecordGroup(String status, ClientDataRecordGroup recordGroup);

	/**
	 * getStatus returns the value of the atomic with nameInData status from inside the recordInfo
	 * group.
	 * 
	 * @param recordGroup
	 *            To get the child status from its recordInfo
	 * @return A String with the value of the status
	 */
	String getStatusFromRecordGroup(ClientDataRecordGroup recordGroup);

	/**
	 * addProcessingMessageInRecordGroup adds the message to recordInfo in the provided recordGroup,
	 * using nameInData processingMessage.
	 * 
	 * @param message
	 *            A String with the message to set
	 * @param recordGroup
	 *            To set the message in processingMessage in its recordInfo
	 */
	void addProcessingMessageInRecordGroup(String message, ClientDataRecordGroup recordGroup);

}
