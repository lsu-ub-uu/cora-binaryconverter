package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.LinkedList;

public class Opj2DecompressImp implements Opj2Decompress {

	@Override
	public int run(Opj2OpsImp ops) {
		LinkedList<String> operations = ops.getOpsList();
		operations.addFirst("opj2_decompress");
		Opj2ProcessRunner runner = new Opj2ProcessRunnerImp(operations);
		return runner.convertImage();
	}
}
