package se.uu.ub.cora.binaryconverter.openjpeg2;

public interface Opj2ProcessBuilder {

	/**
	 * 
	 * 
	 * If error while running the method an {@link OpenJpeg2Exception} will be thrown.
	 * 
	 * @return
	 */
	Process start();

	/**
	 * Logs streams to console
	 * 
	 * @return
	 */
	Opj2ProcessBuilder inheritIO();

}
