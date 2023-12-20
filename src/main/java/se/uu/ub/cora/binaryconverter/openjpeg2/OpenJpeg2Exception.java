package se.uu.ub.cora.binaryconverter.openjpeg2;

public class OpenJpeg2Exception extends RuntimeException {

	private static final long serialVersionUID = -255261285196817577L;

	private OpenJpeg2Exception(String message) {
		super(message);
	}

	private OpenJpeg2Exception(String message, Exception e) {
		super(message, e);
	}

	public static OpenJpeg2Exception withMessage(String message) {
		return new OpenJpeg2Exception(message);
	}

	public static OpenJpeg2Exception withMessageAndException(String message, Exception e) {
		return new OpenJpeg2Exception(message, e);
	}

}
