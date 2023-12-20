package se.uu.ub.cora.binaryconverter.openjpeg2.spy;

import java.io.InputStream;
import java.io.OutputStream;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ProcessSpy extends Process {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public ProcessSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("waitFor", () -> 0);
		MRV.setDefaultReturnValuesSupplier("exitValue", () -> 0);
		MRV.setDefaultReturnValuesSupplier("isAlive", () -> true);
		MRV.setDefaultReturnValuesSupplier("destroyForcibly", ProcessSpy::new);
	}

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public int waitFor() throws InterruptedException {
		return (int) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public int exitValue() {
		return (int) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public void destroy() {
		MCR.addCall();

	}

	@Override
	public boolean isAlive() {
		return (boolean) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public Process destroyForcibly() {
		return (Process) MCR.addCallAndReturnFromMRV();
	}
}
