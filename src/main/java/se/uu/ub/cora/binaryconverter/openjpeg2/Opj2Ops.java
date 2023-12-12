package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.List;

public interface Opj2Ops {

	/**
	 * Get ops list
	 * 
	 * @return The configured list of ops
	 */
	List<String> getOpsList();

	/**
	 * Get input path
	 * 
	 * @return The input path opj2 is set to use
	 */
	String getInputPath();

	/**
	 * Path to the input file (e.g. "/path/to/inputFile")
	 * 
	 * @param inputPath
	 *            Path to the file to convert from.
	 */
	void inputPath(String inputPath);

	/**
	 * Get output path
	 * 
	 * @return The output path opj2 is set to use
	 */
	String getOutputPath();

	/**
	 * Path for the output file (e.g. "/path/to/outputFile") When used for decompress a file
	 * extension is needed
	 * 
	 * @param outputPath
	 *            Path to the file to convert to.
	 */
	void outputPath(String outputPath);

	/**
	 * Different psnr for successive layers (e.g. "25,28,30,35,40") Note: (options psnrQuality and
	 * compressionRatio cannot be used together)
	 * 
	 * @param psnrLayers
	 *            Psnr settings for layers
	 */
	void psnrQuality(String psnrLayers);

	/**
	 * Different compression ratio(s) for successive layers. The rate specified for each quality
	 * level is the desired compression factor (e.g. "2.5"). Note: (options compressionRatio and
	 * psnrQuality cannot be used together)
	 * 
	 * @param ratio
	 *            The ratio for each successive layers
	 */
	void compressionRatio(String ratio);

	/**
	 * Size of tile (e.g. "1024,1024")
	 * 
	 * @param tileSize
	 *            The size of each tile
	 */
	void tileSize(String tileSize);

	/**
	 * Number of resolutions (e.g. 7, Default: 6)
	 * 
	 * @param numOfResolutions
	 *            The number of resolutions
	 */
	void numOfResolutions(int numOfResolutions);

	/**
	 * Size of precinct (e.g. "128,128", Default: 2^15 x 2^15)
	 * 
	 * @param precinctSize
	 *            The precinct size to use
	 */
	void precinctSize(String precinctSize);

	/**
	 * Size of code block (e.g. "64,64", Default: 64 x 64)
	 * 
	 * @param cblSize
	 *            The Code block size to use
	 */
	void codeBlockSize(String cblSize);

	/**
	 * Progression order (e.g. "RPCL", Default: LRCP)
	 * 
	 * @param progressionOrderName
	 *            The name of the progression order to use
	 */
	void progressionOrder(String progressionOrderName);

	/**
	 * Write SOP markers before each packet (default: off)
	 * 
	 */
	void enableSop();

	/**
	 * Write EPH marker after each header packet (default: off)
	 * 
	 */
	void enableEph();

	/**
	 * Write PLT marker in tile-part header
	 * 
	 */
	void enablePlt();

	/**
	 * Write TLM marker in tile-part header
	 * 
	 */
	void enableTlm();

	/**
	 * Divide packets of every tile into tile-parts. Division is made by grouping Resolutions (R),
	 * Layers (L) or Components (C).
	 * 
	 * @param type
	 *            Type of grouping to use (R|L|C)
	 */
	void tilePartDivider(String type);

	/**
	 * Number of threads for opj2 to use (e.g. 6)
	 * 
	 * @param numOfThreads
	 *            The amount of threads the opj2 command should utilize
	 */
	void numberOfThreads(int numOfThreads);
}
