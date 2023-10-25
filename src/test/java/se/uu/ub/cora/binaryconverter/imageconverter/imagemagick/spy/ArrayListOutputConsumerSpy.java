package se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy;

import java.util.ArrayList;

import org.im4java.process.ArrayListOutputConsumer;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ArrayListOutputConsumerSpy extends ArrayListOutputConsumer {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public ArrayListOutputConsumerSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("getOutput", ArrayList<String>::new);
	}

	@Override
	public ArrayList<String> getOutput() {
		return (ArrayList<String>) MCR.addCallAndReturnFromMRV();
	}

}
