package se.uu.ub.cora.binaryconverter.spy;

import java.io.IOException;
import java.util.ArrayList;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.Operation;

import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy.IMOpsSpy;
import se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy.OperationSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ConvertCmdSpy extends ConvertCmd {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();
	public ArrayList<String> callsInOrder = new ArrayList<>();

	public ConvertCmdSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("addImage", OperationSpy::new);
		MRV.setDefaultReturnValuesSupplier("format", IMOpsSpy::new);
	}

	@Override
	public void run(Operation arg0, Object... arg1)
			throws IOException, InterruptedException, IM4JavaException {
		MCR.addCall("arg0", arg0, "arg1", arg1);
	}
}
