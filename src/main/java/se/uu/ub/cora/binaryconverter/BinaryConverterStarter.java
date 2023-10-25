package se.uu.ub.cora.binaryconverter;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageBigConverter;
import se.uu.ub.cora.binaryconverter.imageconverter.ImageSmallConverter;

public class BinaryConverterStarter {

	public static void main(String[] args) {

		// SPIKE STARTS
		String queuName = args[0];
		if (queuName.equals("smallConverterQueue")) {
			new ImageSmallConverter();
		}
		if (queuName.equals("bigConverterQueue")) {
			new ImageBigConverter();
		}
		// SPIKE ENDS
	}

}
