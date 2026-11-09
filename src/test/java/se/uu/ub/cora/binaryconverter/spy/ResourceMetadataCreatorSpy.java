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
package se.uu.ub.cora.binaryconverter.spy;

import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreator;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ResourceMetadataCreatorSpy implements ResourceMetadataCreator {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public ResourceMetadataCreatorSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("createMetadataForRepresentation",
				ClientDataGroupSpy::new);
		MRV.setDefaultReturnValuesSupplier("getStatusFromRecordGroup", () -> "");
	}

	@Override
	public ClientDataGroup createMetadataForRepresentation(String pathToStream, String recordId,
			String representation, String mimeType, ImageData imageData) {
		return (ClientDataGroup) MCR.addCallAndReturnFromMRV("pathToStream", pathToStream,
				"recordId", recordId, "representation", representation, "mimeType", mimeType,
				"imageData", imageData);
	}

	@Override
	public void updateMasterGroup(ClientDataRecordGroup recordGroup, ImageData imageData) {
		MCR.addCall("recordGroup", recordGroup, "imageData", imageData);
	}

	@Override
	public void setChildInRecordGroup(ClientDataGroup child, ClientDataRecordGroup recordGroup) {
		MCR.addCall("child", child, "recordGroup", recordGroup);
	}

	@Override
	public void setStatusInRecordGroup(String status, ClientDataRecordGroup recordGroup) {
		MCR.addCall("status", status, "recordGroup", recordGroup);
	}

	@Override
	public String getStatusFromRecordGroup(ClientDataRecordGroup recordGroup) {
		return (String) MCR.addCallAndReturnFromMRV("recordGroup", recordGroup);
	}

	@Override
	public void addProcessingMessageInRecordGroup(String message,
			ClientDataRecordGroup recordGroup) {
		MCR.addCall("message", message, "recordGroup", recordGroup);
	}
}