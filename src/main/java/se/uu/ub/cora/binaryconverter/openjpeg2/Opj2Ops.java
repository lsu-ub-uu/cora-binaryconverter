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
	 * Different psnr for successive layers (e.g. 30,40,50).<br>
	 * <br>
	 * 
	 * Increasing PSNR values required, except 0 which can be used for the last layer to indicate it
	 * is lossless. <br>
	 * Note: (options psnrQuality and compressionRatio cannot be used together)
	 * 
	 * @param psnrLayers
	 *            Psnr settings for layers
	 */
	void psnrQuality(int... psnrLayers);

	/**
	 * The rate specified for each quality level is the desired compression factor (use 1 for
	 * lossless) Decreasing ratios required. <br>
	 * <br>
	 * 
	 * Example: "20,10,1":
	 * <ul>
	 * <li>quality layer 1: compress 20x</li>
	 * <li>quality layer 2: compress 10x</li>
	 * <li>quality layer 3: compress lossless</li>
	 * </ul>
	 * Note: (options compressionRatio and psnrQuality cannot be used together)
	 * 
	 * @param ratio
	 *            The ratio for each successive layers
	 */
	void compressionRatio(int... ratio);

	/**
	 * Size of tile (e.g. "1024,1024")<br>
	 * <br>
	 * Default: the dimension of the whole image, meaning the whole image.
	 * 
	 * @param width
	 *            The width of each tile
	 * @param height
	 *            The height of each tile
	 */
	void tileSize(int width, int height);

	/**
	 * Number of resolutions<br>
	 * <br>
	 * 
	 * Should be relative to the image size. As an example 5 levels should be enough for 2000x2000,
	 * 6 levels for 4000x4000 and 7 levels for 8000x8000 and so forth. <br>
	 * <br>
	 * It corresponds to the number of DWT decompositions +1<br>
	 * Default: 6
	 * 
	 * @param numOfResolutions
	 *            The number of resolutions
	 */
	void numOfResolutions(int numOfResolutions);

	/**
	 * Precinct size. Values specified must be power of 2. Multiple records may be supplied, in
	 * which case the first record refers to the highest resolution level and subsequent records to
	 * lower resolution levels. The last specified record is halved successively for each remaining
	 * lower resolution levels.<br>
	 * <br>
	 * 
	 * Precincts allow decoding for specific regions, not unlike how tiling works for TIFF. For
	 * tile-based viewers a value of 256x256 is usually recommended.<br>
	 * <br>
	 * 
	 * Default: 2^15x2^15 at each resolution.
	 * 
	 * @param width
	 *            The precinct width to use
	 * @param height
	 *            The precinct height to use
	 */
	void precinctSize(int width, int height);

	/**
	 * Code-block size. The dimension must respect the constraint defined in the JPEG-2000 standard
	 * (no dimension smaller than 4 or greater than 1024, no code-block with more than 4096
	 * coefficients).<br>
	 * <br>
	 * 
	 * Code block is used to assist with random access and should optimally be as large as possible.
	 * Maximum allowed size is 64x64.
	 * 
	 * @param width
	 *            The Code block width to use
	 * @param height
	 *            The Code block height to use
	 * 
	 */
	void codeBlockSize(int width, int height);

	/**
	 * Progression order<br>
	 * <br>
	 * For efficient access to different resolutions the recommended settings is "RCPL" (Resolution,
	 * Component, Position, Layer)<br>
	 * <br>
	 * Default: LRCP
	 * 
	 * @param progressionOrderName
	 *            The name of the progression order to use (LRCP|RLCP|RPCL|PCRL|CPRL)
	 */
	void progressionOrder(String progressionOrderName);

	/**
	 * Write SOP markers before each packet (default: off)<br>
	 * <br>
	 * 
	 * Provides error resiliency and can assist decoding of corrupt files.
	 * 
	 */
	void enableSop();

	/**
	 * Write EPH marker after each header packet (default: off)<br>
	 * <br>
	 * 
	 * Provides error resiliency and can assist decoding of corrupt files.
	 * 
	 */
	void enableEph();

	/**
	 * Write PLT marker in tile-part header<br>
	 * <br>
	 * 
	 * PLT markers allow faster access to different resolution levels and regions.
	 * 
	 */
	void enablePlt();

	/**
	 * Write TLM marker in tile-part header<br>
	 * <br>
	 * 
	 * TLM markers provides a coarse index that can assist decoders without PLT support
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
