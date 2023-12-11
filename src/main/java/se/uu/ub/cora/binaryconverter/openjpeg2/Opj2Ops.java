package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.LinkedList;

public interface Opj2Ops {

	/**
	 * Get ops list
	 */
	LinkedList<String> getOpsList();

	/**
	 * Get input path
	 */
	String getInputPath();

	/**
	 * Path to the input file (e.g. "/path/to/inputFile")
	 */
	void inputPath(String inputPath);

	/**
	 * Get output path
	 */
	String getOutputPath();

	/**
	 * Path for the output file (e.g. "/path/to/outputFile") When used for decompress a file
	 * extension is needed
	 */
	void outputPath(String outputPath);

	/**
	 * different psnr for successive layers (e.g. "25,28,30,35,40")
	 */
	void psnrQuality(String psnrLayers);

	/**
	 * Size of tile (e.g. "1024,1024")
	 */
	void tileSize(String tileSize);

	/**
	 * Number of resolutions (e.g. 7, Default: 6)
	 */
	void numOfResolutions(int numOfResolutions);

	/**
	 * Size of precinct (e.g. "128,128", Default: 2^15 x 2^15)
	 */
	void precinctSize(String precinctSize);

	/**
	 * Size of code block (e.g. "64,64", Default: 64 x 64)
	 */
	void codeBlockSize(String cblSize);

	/**
	 * Progression order (e.g. "RPCL", Default: LRCP)
	 */
	void progressionOrder(String progressionOrderName);

	/**
	 * Write SOP markers before each packet (default: off)
	 */
	void enableSop(boolean enableSop);

	/**
	 * Write EPH marker after each header packet (default: off)
	 */
	void enableEph(boolean enableEph);
}
