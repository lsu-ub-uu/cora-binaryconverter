package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;
import java.util.List;

public class Opj2CommandImp implements Opj2Command {

	@Override
	public void compress(Opj2Ops ops) throws OpenJpeg2Exception, InterruptedException, IOException {
		List<String> options = ops.getOpsList();
		options.add(0, "opj2_compress");
		runCommand(options);
	}

	@Override
	public void decompress(Opj2Ops ops)
			throws OpenJpeg2Exception, InterruptedException, IOException {
		List<String> options = ops.getOpsList();
		options.add(0, "opj2_decompress");
		runCommand(options);
	}

	private void runCommand(List<String> options)
			throws OpenJpeg2Exception, InterruptedException, IOException {
		ProcessBuilder builder = configureBuilder(options);
		Opj2ProcessRunner runner = new Opj2ProcessRunnerImp(builder);
		runner.runOpj2Process();
	}

	private ProcessBuilder configureBuilder(List<String> options) {
		ProcessBuilder builder = new ProcessBuilder(options);
		builder.inheritIO(); // send logs to console, use redirect.... to send elsewhere (file etc)
		return builder;
	}
}