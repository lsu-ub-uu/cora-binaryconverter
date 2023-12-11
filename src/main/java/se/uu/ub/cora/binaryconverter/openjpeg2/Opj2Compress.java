package se.uu.ub.cora.binaryconverter.openjpeg2;

public interface Opj2Compress {

	/**
	 * Compress to JPEG20000 using opj2_compress
	 * Valid input image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw, .tga, .tif . For PNG resp. TIF it needs libpng resp. libtiff.
     * Valid output image extensions are .j2k, .jp2
	 */
	int run(Opj2Ops ops);
}
