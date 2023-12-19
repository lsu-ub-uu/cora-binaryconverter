package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;
import java.util.List;

public class Opj2CommandImp implements Opj2Command {

	@Override
	public void compress(Opj2Ops ops) throws OpenJpeg2Exception, InterruptedException, IOException {
		List<String> operations = ops.getOpsList();
		operations.add(0, "opj2_compress");
		runCommand(operations);
	}

	@Override
	public void decompress(Opj2Ops ops)
			throws OpenJpeg2Exception, InterruptedException, IOException {
		List<String> operations = ops.getOpsList();
		operations.add(0, "opj2_decompress");
		runCommand(operations);
	}

	private void runCommand(List<String> operations)
			throws OpenJpeg2Exception, InterruptedException, IOException {
		ProcessBuilder builder = configureBuilder(operations);
		Opj2ProcessRunner runner = new Opj2ProcessRunnerImp(builder);
		runner.runOpj2Process();
	}

	private ProcessBuilder configureBuilder(List<String> operations) {
		ProcessBuilder builder = new ProcessBuilder(operations);
		builder.inheritIO(); // send logs to console, use redirect.... to send elsewhere (file etc)
		return builder;
	}
}