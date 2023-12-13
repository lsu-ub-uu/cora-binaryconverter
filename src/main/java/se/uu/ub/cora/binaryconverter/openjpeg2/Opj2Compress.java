package se.uu.ub.cora.binaryconverter.openjpeg2;

/**
 * Compress to JPEG20000 using opj2_compress<br>
 * <br>
 * 
 * Default encoding options:
 * <ul>
 * <li>Lossless</li>
 * <li>1 tile</li>
 * <li>RGB->YCC conversion if at least 3 components</li>
 * <li>Size of precinct : 2^15 x 2^15 (means 1 precinct)</li>
 * <li>Size of code-block : 64 x 64</li>
 * <li>Number of resolutions: 6</li>
 * <li>No SOP marker in the codestream</li>
 * <li>No EPH marker in the codestream</li>
 * <li>No sub-sampling in x or y direction</li>
 * <li>No mode switch activated</li>
 * <li>Progression order: LRCP</li>
 * <li>No ROI upshifted</li>
 * <li>No offset of the origin of the image</li>
 * <li>No offset of the origin of the tiles</li>
 * <li>Reversible DWT 5-3</li>
 * </ul>
 * 
 * Valid input image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw, .tga, .tif<br>
 * Valid output image extensions are .j2k, .jp2
 */
public interface Opj2Compress {

	/**
	 * Compress to JPEG20000 using opj2_compress<br>
	 * <br>
	 * Note: The markers written to the main_header are : SOC SIZ COD QCD COM. COD and QCD never
	 * appear in the tile_header. <br>
	 * <br>
	 * 
	 * @param ops
	 *            The ops opj2_compress should use to run
	 * 
	 * @return True if convertion was successful
	 */
	boolean run(Opj2Ops ops);
}
