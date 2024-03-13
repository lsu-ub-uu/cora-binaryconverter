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
package se.uu.ub.cora.binaryconverter.imagemagick.image;

import java.io.IOException;
import java.util.List;

import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;

import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

public class ImageAnalyzerImp implements ImageAnalyzer {

	private static final int IMAGE_DATA_RESOLUTION = 0;
	private static final int IMAGE_DATA_WIDTH = 1;
	private static final int IMAGE_DATA_HEIGHT = 2;
	private static final int IMAGE_DATA_SIZE = 3;
	private static final String FORMAT_DPI_WIDTH_HEIGHT_SIZE = "%xx%y %w %h %B";
	private static final String SPLIT_REGEX = " ";
	private String imagePath;

	IdentifyCmd identifyCmd = new IdentifyCmd();
	IMOperation imOperation = new IMOperation();
	ArrayListOutputConsumer outputConsumer = new ArrayListOutputConsumer();

	public ImageAnalyzerImp(String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public ImageData analyze() {
		IMOps format = addImageAndSetFormat(imagePath);
		return tryToAnalyzeImageUsingImageMagick(format);
	}

	private ImageData tryToAnalyzeImageUsingImageMagick(IMOps format) {
		try {
			List<String> output = executeAnalyzeCommandInImageMagick(format);
			return parseImageData(output);
		} catch (Exception e) {
			throw BinaryConverterException.withMessageAndException(
					"Error when analyzing image, with path: " + imagePath, e);
		}
	}

	private List<String> executeAnalyzeCommandInImageMagick(IMOps format)
			throws IOException, InterruptedException, IM4JavaException {
		identifyCmd.setOutputConsumer(outputConsumer);
		identifyCmd.run(format);
		return outputConsumer.getOutput();
	}

	private IMOps addImageAndSetFormat(String imagePath) {
		imOperation.format(FORMAT_DPI_WIDTH_HEIGHT_SIZE);
		imOperation.addImage(imagePath);
		return imOperation;
	}

	private ImageData parseImageData(List<String> result) {
		String[] imageData = prepareOutputFromImageMagick(result);
		return new ImageData(imageData[IMAGE_DATA_RESOLUTION], imageData[IMAGE_DATA_WIDTH],
				imageData[IMAGE_DATA_HEIGHT], imageData[IMAGE_DATA_SIZE]);
	}

	private String[] prepareOutputFromImageMagick(List<String> result) {
		String rawOutput = result.get(0);
		return rawOutput.split(SPLIT_REGEX);
	}

	public void onlyForTestSetIdentifyCmd(IdentifyCmd identifyCmd) {
		this.identifyCmd = identifyCmd;
	}

	public void onlyForTestSetIMOperation(IMOperation imOperation) {
		this.imOperation = imOperation;
	}

	public void onlyForTestSetArrayListOutputConsumer(ArrayListOutputConsumer outputConsumer) {
		this.outputConsumer = outputConsumer;
	}

	public String onlyForTestGetImagePath() {
		return imagePath;
	}

}
