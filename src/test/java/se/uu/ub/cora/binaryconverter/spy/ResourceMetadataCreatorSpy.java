package se.uu.ub.cora.binaryconverter.spy;

import se.uu.ub.cora.binaryconverter.common.ResourceMetadataCreator;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ResourceMetadataCreatorSpy implements ResourceMetadataCreator {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public ResourceMetadataCreatorSpy() {
		MCR.useMRV(MRV);
	}

	@Override
	public void createMetadataForRepresentation(String representation,
			ClientDataGroup resourceInfoGroup, String recordId, ImageData imageData) {
		MCR.addCall("representation", representation, "resourceInfoGroup", resourceInfoGroup,
				"recordId", recordId, "imageData", imageData);

	}

	@Override
	public void updateMasterGroupFromResourceInfo(ClientDataGroup resourceInfoGroup,
			ImageData imageData) {
		MCR.addCall("resourceInfoGroup", resourceInfoGroup, "imageData", imageData);

	}

}
