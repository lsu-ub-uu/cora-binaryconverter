package se.uu.ub.cora.binaryconverter.image;

import se.uu.ub.cora.binaryconverter.common.BinaryConverterException;

public interface Jp2Converter {

	/**
	 * convert a master image to a jpeg2000 image.
	 * 
	 * @param inputPath
	 *            Path to the file to convert from.
	 * @param outputPath
	 *            Path where to store the converted file.
	 *
	 * @throws BinaryConverterException
	 *             if the conversion fails
	 */
	void convert(String inputPath, String outputPath);
}
