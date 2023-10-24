package se.uu.ub.cora.binaryconverter.imageconverter.spy;

import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;
import org.im4java.core.Operation;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class IMOperationSpy extends IMOperation {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public IMOperationSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("addImage", OperationSpy::new);
		MRV.setDefaultReturnValuesSupplier("format", IMOpsSpy::new);
	}

	@Override
	public Operation addImage(String... arg0) {
		return (Operation) MCR.addCallAndReturnFromMRV("arg0", arg0);
	}

	@Override
	public IMOps format(String arg0) {
		return (IMOps) MCR.addCallAndReturnFromMRV("arg0", arg0);
	}

}
