package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;
import java.util.List;

public class Opj2CommandImp implements Opj2Command {

	@Override
	public void compress(Opj2Parameters parameters) throws OpenJpeg2Exception, InterruptedException, IOException {
		List<String> params = parameters.getParamsList();
		params.add(0, "opj2_compress");
		runCommand(params);
	}

	@Override
	public void decompress(Opj2Parameters parameters)
			throws OpenJpeg2Exception, InterruptedException, IOException {
		List<String> params = parameters.getParamsList();
		params.add(0, "opj2_decompress");
		runCommand(params);
	}

	private void runCommand(List<String> parameters)
			throws OpenJpeg2Exception, InterruptedException, IOException {
		ProcessBuilder builder = configureBuilder(parameters);
		Opj2ProcessRunner runner = new Opj2ProcessRunnerImp(builder);
		runner.runOpj2Process();
	}

	private ProcessBuilder configureBuilder(List<String> parameters) {
		ProcessBuilder builder = new ProcessBuilder(parameters);
		builder.inheritIO(); // send logs to console, use redirect.... to send elsewhere (file etc)
		return builder;
	}
}