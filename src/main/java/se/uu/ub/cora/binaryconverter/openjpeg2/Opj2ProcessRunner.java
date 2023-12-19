package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;

public interface Opj2ProcessRunner {

	/**
	 * Run the opj2 (compress or decompress) command using settings supplied via Opj2Ops
	 * 
	 * @return The exit code for the opj2 process
	 * @throws OpenJpeg2Exception
	 * @throws InterruptedException
	 * @throws IOException
	 */
	void runOpj2Process() throws OpenJpeg2Exception, InterruptedException, IOException;
}
