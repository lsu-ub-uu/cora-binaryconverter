package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;

/**
 * Run either the openjpeg2-tools commands compress or decompress
 * 
 */
public interface Opj2Command {

	/**
	 * Compress to JPEG2000 using opj2_compress<br>
	 * 
	 * Default encoding options if no Opj2Parameters are defined:<br>
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
	 * <br>
	 * Note: The markers written to the main_header are : SOC SIZ COD QCD COM. COD and QCD never
	 * appear in the tile_header. <br>
	 * <br>
	 * Valid input image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw, .tga, .tif<br>
	 * Valid output image extensions are .j2k, .jp2
	 * 
	 * @param parameters
	 *            The parameters opj2_compress should use to run
	 * @throws OpenJpeg2Exception
	 * @throws InterruptedException
	 * @throws IOException
	 */
	void compress(Opj2Parameters parameters)
			throws OpenJpeg2Exception, InterruptedException, IOException;

	/**
	 * Decompress a JPEG2000 using opj2_decompress<br>
	 * Note: For decompress the only available parameters are inputPath, outputPath and threads<br>
	 * <br>
	 * Valid input image extensions are .j2k, .jp2, .j2c, .jpt<br>
	 * Valid output image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw, .tga, .tif
	 * 
	 * @param parameters
	 *            The parameters opj2_decompress should use to run
	 * @throws OpenJpeg2Exception
	 * @throws InterruptedException
	 * @throws IOException
	 */
	void decompress(Opj2Parameters parameters)
			throws OpenJpeg2Exception, InterruptedException, IOException;
}
