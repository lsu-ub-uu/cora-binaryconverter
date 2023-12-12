package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.List;

public class Opj2CompressImp implements Opj2Compress {

	@Override
	public int run(Opj2Ops ops) {
		List<String> operations = ops.getOpsList();
		operations.add(0, "opj2_compress");
		Opj2ProcessRunner runner = new Opj2ProcessRunnerImp(operations);
		return runner.convertImage();
	}
}