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
	 * Different psnr for successive layers (e.g. "30,40,50").
     * Increasing PSNR values required, except 0 which can be used for the last
     * layer to indicate it is lossless. 
	 * Note: (options psnrQuality and compressionRatio cannot be used together)
	 * 
	 * @param psnrLayers
	 *            Psnr settings for layers
	 */
	void psnrQuality(String psnrLayers);

	/**
	 * The rate specified for each quality level is the desired compression factor (use 1 for
	 * lossless) Decreasing ratios required. 
	 * Example: "20,10,1" means 
	 * quality layer 1: compress 20x, 
	 * quality layer 2: compress 10x 
	 * quality layer 3: compress lossless 
	 * Note: (options compressionRatio and psnrQuality cannot be used together)
	 * 
	 * @param ratio
	 *            The ratio for each successive layers
	 */
	void compressionRatio(String ratio);

	/**
	 * Size of tile (e.g. "1024,1024")
	 * Default: the dimension of the whole image, thus only one tile.
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
	 * Precinct size. Values specified must be power of 2.
     * Multiple records may be supplied, in which case the first record refers
     * to the highest resolution level and subsequent records to lower
     * resolution levels. The last specified record is halved successively for each
     * remaining lower resolution levels.
     * Default: 2^15x2^15 at each resolution.
	 * 
	 * @param precinctSize
	 *            The precinct size to use
	 */
	void precinctSize(String precinctSize);

	/**
	 * Code-block size. The dimension must respect the constraint
     * defined in the JPEG-2000 standard (no dimension smaller than 4
     * or greater than 1024, no code-block with more than 4096 coefficients).
     * The maximum value authorized is 64x64.
	 * 
	 * @param cblSize
	 *            The Code block size to use
	 */
	void codeBlockSize(String cblSize);

	/**
	 * Progression order (e.g. "RPCL", Default: LRCP)
	 * 
	 * @param progressionOrderName
	 *            The name of the progression order to use (LRCP|RLCP|RPCL|PCRL|CPRL)
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
