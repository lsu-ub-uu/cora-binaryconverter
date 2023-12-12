package se.uu.ub.cora.binaryconverter.openjpeg2;

public interface Opj2Compress {

	/**
	 * Compress to JPEG20000 using opj2_compress Valid input image extensions are .bmp, .pgm, .pgx,
	 * .png, .pnm, .ppm, .raw, .tga, .tif . For PNG resp. TIF it needs libpng resp. libtiff. Valid
	 * output image extensions are .j2k, .jp2
	 * 
	 * Default encoding options:
	 * Lossless
	 * 1 tile
	 * RGB->YCC conversion if at least 3 components
	 * Size of precinct : 2^15 x 2^15 (means 1 precinct)
	 * Size of code-block : 64 x 64
	 * Number of resolutions: 6
	 * No SOP marker in the codestream
	 * No EPH marker in the codestream
	 * No sub-sampling in x or y direction
	 * No mode switch activated
	 * Progression order: LRCP
	 * No ROI upshifted
	 * No offset of the origin of the image
	 * No offset of the origin of the tiles
	 * Reversible DWT 5-3
	 * 
	 * Note:
	 * The markers written to the main_header are : SOC SIZ COD QCD COM.
	 * COD and QCD never appear in the tile_header.
	 * 
	 * @param ops
	 *            The ops opj2_compress should use to run
	 * 
	 * @return True if convertion was successful
	 */
	boolean run(Opj2Ops ops);
}
