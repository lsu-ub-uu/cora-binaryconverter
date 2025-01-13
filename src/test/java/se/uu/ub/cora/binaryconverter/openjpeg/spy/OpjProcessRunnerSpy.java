package se.uu.ub.cora.binaryconverter.openjpeg.spy;

import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjProcessRunner;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class OpjProcessRunnerSpy implements OpjProcessRunner {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public OpjProcessRunnerSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("runOpjProcess", OpjProcessRunnerSpy::new);
	}

	@Override
	public void runOpjProcess() {
		MCR.addCall();
	}
}
