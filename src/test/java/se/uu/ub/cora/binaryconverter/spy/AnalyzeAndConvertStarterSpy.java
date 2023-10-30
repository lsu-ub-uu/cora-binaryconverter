package se.uu.ub.cora.binaryconverter.spy;

import se.uu.ub.cora.binaryconverter.imageconverter.AnalyzeAndConvertStarter;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class AnalyzeAndConvertStarterSpy implements AnalyzeAndConvertStarter {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public AnalyzeAndConvertStarterSpy() {
		MCR.useMRV(MRV);
	}

	@Override
	public void listen() {
		MCR.addCall();
	}
}
