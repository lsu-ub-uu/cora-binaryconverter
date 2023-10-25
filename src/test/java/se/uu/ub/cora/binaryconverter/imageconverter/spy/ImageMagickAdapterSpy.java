package se.uu.ub.cora.binaryconverter.imageconverter.spy;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageData;
import se.uu.ub.cora.binaryconverter.imageconverter.ImageMagickAdapter;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ImageMagickAdapterSpy implements ImageMagickAdapter {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	ImageData imageData = new ImageData("someResolution", "someWidth", "someHeight");

	public ImageMagickAdapterSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("analyze", () -> imageData);
	}

	@Override
	public ImageData analyze(String imagePath) {
		return (ImageData) MCR.addCallAndReturnFromMRV("imagePath", imagePath);
	}

}
