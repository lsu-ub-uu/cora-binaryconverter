package se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.imageconverter.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ImageAnalyzerFactorySpy implements ImageAnalyzerFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public ImageAnalyzerFactorySpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factor", ImageAnalyzerSpy::new);
	}

	@Override
	public ImageAnalyzer factor(String path) {
		return (ImageAnalyzer) MCR.addCallAndReturnFromMRV("path", path);
	}
}
