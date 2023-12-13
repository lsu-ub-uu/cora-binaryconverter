package se.uu.ub.cora.binaryconverter.openjpeg2;

public interface Opj2ProcessRunner {

	/**
	 * Run the opj2 (compress or decompress) command using settings supplied via Opj2Ops
	 * 
	 * @return The exit code for the opj2 process
	 */
	int runOpj2Process();
}
