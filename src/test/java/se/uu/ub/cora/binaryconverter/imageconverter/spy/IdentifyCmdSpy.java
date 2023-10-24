package se.uu.ub.cora.binaryconverter.imageconverter.spy;

import java.io.IOException;

import org.im4java.core.IM4JavaException;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.Operation;
import org.im4java.process.OutputConsumer;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class IdentifyCmdSpy extends IdentifyCmd {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public IdentifyCmdSpy() {
		MCR.useMRV(MRV);
	}

	@Override
	public void run(Operation arg0, Object... arg1)
			throws IOException, InterruptedException, IM4JavaException {
		MCR.addCall("arg0", arg0, "arg1", arg1);
	}

	@Override
	public void setOutputConsumer(OutputConsumer arg0) {
		/**
		 * IdentifyCmd extends ImageCommand class which in its constructor calls setOutputConsumer.
		 * When this happens MCR is still null since IndentifyCmdSpy code has not been initialized
		 * yet. We MITIGATE the problem but initializing MCR in this method.
		 */
		if (MCR == null) {
			MCR = new MethodCallRecorder();
		}
		MCR.addCall("arg0", arg0);
	}

}
