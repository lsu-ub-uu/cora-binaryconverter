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
	}

	@Override
	public void updateMasterGroup(ClientDataGroup masterGroup,
			ImageData imageData) {
		MCR.addCall("resourceInfoGroup", masterGroup, "imageData", imageData);

	}

	@Override
	public ClientDataGroup createMetadataForRepresentation(String representation, String recordId,
			ImageData imageData, String mimeType) {
		return (ClientDataGroup) MCR.addCallAndReturnFromMRV("representation", representation,
				"recordId", recordId, "imageData", imageData, "mimeType", mimeType);
	}
}