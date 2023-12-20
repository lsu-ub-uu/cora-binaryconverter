package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;
import java.util.List;

public class Opj2ProcessBuilderImp implements Opj2ProcessBuilder {

	ProcessBuilder processBuilder;

	public Opj2ProcessBuilderImp(List<String> commands) {
		processBuilder = new ProcessBuilder(commands);
	}

	@Override
	public Process start() {
		try {
			return runStart();
		} catch (IOException e) {
			throw OpenJpeg2Exception.withMessage(e.getMessage());
		}
	}

	Process runStart() throws IOException {
		return processBuilder.start();
	}

	ProcessBuilder onlyForTestGetProcessBuilder() {
		return processBuilder;
	}

	@Override
	public Opj2ProcessBuilder inheritIO() {
		processBuilder.inheritIO();
		return this;
	}

}
