package se.uu.ub.cora.binaryconverter.image;

public interface Jp2Converter {

	/**
	 * convert a master image to a jpeg2000 image.
	 * 
	 * @param inputPath
	 *            Path to the file to convert from.
	 * @param outputPath
	 *            Path where to store the converted file.
	 *
	 * @throws ImageConverterException
	 *             if the conversion fails
	 */
	void convert(String inputPath, String outputPath);
}
