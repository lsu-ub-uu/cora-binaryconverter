package se.uu.ub.cora.binaryconverter.imageconverter;

public class ImageConverterException extends RuntimeException {

	private static final long serialVersionUID = -255261285196817577L;

	private ImageConverterException(String message) {
		super(message);
	}

	private ImageConverterException(String message, Exception e) {
		super(message, e);
	}

	public static ImageConverterException withMessage(String message) {
		return new ImageConverterException(message);
	}

	public static ImageConverterException withMessageAndException(String message, Exception e) {
		return new ImageConverterException(message, e);
	}

}
