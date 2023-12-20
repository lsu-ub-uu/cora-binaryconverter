package se.uu.ub.cora.binaryconverter.openjpeg2.spy;

import se.uu.ub.cora.binaryconverter.openjpeg2.Opj2ProcessBuilder;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class Opj2ProcessBuilderSpy implements Opj2ProcessBuilder {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public Opj2ProcessBuilderSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("start", ProcessSpy::new);
		MRV.setDefaultReturnValuesSupplier("start", Opj2ProcessBuilderSpy::new);
	}

	@Override
	public Process start() {
		return (Process) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public Opj2ProcessBuilder inheritIO() {
		return (Opj2ProcessBuilder) MCR.addCallAndReturnFromMRV();
	}
}