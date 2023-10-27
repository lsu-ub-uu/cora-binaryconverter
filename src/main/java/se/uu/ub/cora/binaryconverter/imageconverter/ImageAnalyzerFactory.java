package se.uu.ub.cora.binaryconverter.imageconverter;

public interface ImageAnalyzerFactory {

	/**
	 * Factor method creates a new ImageAnalyzer using a path to an image.
	 * 
	 * @param path
	 *            is a String with the absolute path to an image to analyze.
	 * @return an {@link ImageAnalyzer} is an object that can be use to analyze pictures.
	 */
	public ImageAnalyzer factor(String path);

}