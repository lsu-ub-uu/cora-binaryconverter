/*
 * Copyright 2023 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.binaryconverter.openjpeg.adapter;

/**
 * Run either the openjpeg2-tools commands compress or decompress
 * 
 */
public interface OpjCommand {

	/**
	 * Compress to JPEG2000 using opj_compress<br>
	 * 
	 * Default encoding options if no OpjParameters are defined:<br>
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
	 * Valid input image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .tga, .tif<br>
	 * Valid output image extensions are .j2k, .jp2
	 * 
	 * @param parameters
	 *            The parameters opj_compress should use to run
	 * @throws OpenJpegException
	 *             When something goes wrong
	 * 
	 */
	void compress(OpjParameters parameters);

	/**
	 * Decompress a JPEG2000 using opj_decompress<br>
	 * Note: For decompress the only available parameters are inputPath, outputPath and threads<br>
	 * <br>
	 * Valid input image extensions are .j2k, .jp2, .j2c, .jpt<br>
	 * Valid output image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .tga, .tif
	 * 
	 * @param parameters
	 *            The parameters opj_decompress should use to run
	 * @throws OpenJpegException
	 *             When something goes wrong
	 * 
	 */
	void decompress(OpjParameters parameters);
}
