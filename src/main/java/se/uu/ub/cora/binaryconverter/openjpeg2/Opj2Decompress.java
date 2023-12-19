package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;

/**
 * /** Decompress a JPEG2000 image using opj2_decompress.<br>
 * <br>
 * Valid input image extensions are .j2k, .jp2, .j2c, .jpt<br>
 * Valid output image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw, .tga, .tif
 */
public interface Opj2Decompress {

	/**
	 * Decompress a JPEG2000 image using opj2_decompress.<br>
	 * 
	 * @param ops
	 *            The ops opj2_decompress should use to run. <br>
	 *            Note: Decompress can only utilize <i>inputPath</i>, <i>outputPath</i> and
	 *            <i>threads</i> from the available ops.
	 * @throws OpenJpeg2Exception
	 * @throws InterruptedException
	 * @throws IOException
	 */
	void run(Opj2Ops ops) throws OpenJpeg2Exception, InterruptedException, IOException;
}
