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
package se.uu.ub.cora.binaryconverter.imageconverter.imagemagick;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;

public class PdfConverterImp {

	private String inputPath;

	private ConvertCmd convertCmd = new ConvertCmd();
	IdentifyCmd identifyCmd = new IdentifyCmd();
	private IMOperation imOperation = new IMOperation();
	ArrayListOutputConsumer outputConsumer = new ArrayListOutputConsumer();
	private String outputPath;

	public PdfConverterImp(String inputPath, String outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
	}

	@Override
	public void convertToPDF() {
		// Replace these paths with the actual paths of ImageMagick's convert, your input PDF, and
		// the output thumbnail
		String convertPath = "/path/to/convert";
		String inputPdfPath = "/path/to/input.pdf";
		String outputThumbnailPath = "/path/to/output_thumbnail.png";

		// Create a new ConvertCmd
		ConvertCmd cmd = new ConvertCmd();

		// Create a new IMOperation
		IMOperation op = new IMOperation();

		// Specify the input PDF and the page (0 for the first page)
		op.addImage(inputPdfPath + "[0]");

		// Resize the image to a width of 200 pixels while maintaining the aspect ratio
		op.thumbnail(200);

		// Specify the output thumbnail file
		op.addImage(outputThumbnailPath);

		try {
			// Execute the operation
			cmd.run(op);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void onlyForTestSetConvertCmd(ConvertCmd convertCmd) {
		this.convertCmd = convertCmd;

	}

	void onlyForTestSetIMOperation(IMOperation imOperation) {
		this.imOperation = imOperation;
	}

	void onlyForTestSetArrayListOutputConsumer(ArrayListOutputConsumer outputConsumer) {
		this.outputConsumer = outputConsumer;
	}

	String onlyForTestGetImagePath() {
		return inputPath;
	}
}
