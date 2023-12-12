package se.uu.ub.cora.binaryconverter.openjpeg2;

public interface Opj2ProcessRunner {

	/**
	 * Convert an image using settings supplied via Opj2Ops
	 * 
	 * @return The exit code for the opj2 process
	 */
	int runOpj2Process();
}
