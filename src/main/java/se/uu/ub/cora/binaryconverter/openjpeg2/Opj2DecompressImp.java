package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.List;

public class Opj2DecompressImp implements Opj2Decompress {

	@Override
	public boolean run(Opj2Ops ops) {
		List<String> operations = ops.getOpsList();
		operations.add(0, "opj2_decompress");
		Opj2ProcessRunner runner = new Opj2ProcessRunnerImp(operations);
		return runner.runOpj2Process() == 0;
	}
}
