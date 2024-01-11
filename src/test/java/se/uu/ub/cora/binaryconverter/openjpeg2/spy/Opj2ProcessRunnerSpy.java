package se.uu.ub.cora.binaryconverter.openjpeg2.spy;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessRunner;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class Opj2ProcessRunnerSpy implements Opj2ProcessRunner {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public Opj2ProcessRunnerSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("runOpj2Process", Opj2ProcessRunnerSpy::new);
	}

	@Override
	public void runOpj2Process() {
		MCR.addCall();
	}
}
