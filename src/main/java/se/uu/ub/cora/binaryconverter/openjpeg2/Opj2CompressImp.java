package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;
import java.util.List;

public class Opj2CompressImp implements Opj2Compress {

	@Override
	public void run(Opj2Ops ops) throws OpenJpeg2Exception, InterruptedException, IOException {
		List<String> operations = ops.getOpsList();
		operations.add(0, "opj2_compress");
		Opj2ProcessRunner runner = new Opj2ProcessRunnerImp(operations);
		runner.runOpj2Process();
	}
}