package se.uu.ub.cora.binaryconverter.openjpeg2;

public interface Opj2Decompress {

	/**
	 * Decompress a JPEG2000 image using opj2_decompress Valid input image extensions are .j2k,
	 * .jp2, .j2c, .jpt Valid output image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw,
	 * .tga, .tif . For PNG resp. TIF it needs libpng resp. libtiff .
	 * 
	 * @param ops
	 *            The ops opj2_decompress should use to run
	 * 
	 * @return True if convertion was successful
	 */
	boolean run(Opj2Ops ops);
}
