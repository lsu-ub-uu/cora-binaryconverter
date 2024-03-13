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

import java.text.MessageFormat;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

public class ImageConverterImp implements ImageConverter {
	private static final double QUALITY = 90.0;
	private IMOperationFactory imOperationFactory;
	private ConvertCmd convertCmd;

	public ImageConverterImp(IMOperationFactory imOperationFactory, ConvertCmd convertCmd) {
		this.imOperationFactory = imOperationFactory;
		this.convertCmd = convertCmd;
	}

	@Override
	public void convertAndResizeUsingWidth(String inputPath, String outputPath, int width) {
		IMOperation imOperation = createIMOperationUsingInputPath(inputPath);
		imOperation.resize(width);
		imOperation.quality(QUALITY);
		imOperation.addImage("JPEG:" + outputPath);

		String message = createErrorMessageConvertAndResizeToJpeg(inputPath, width);
		tryToRunImageMagickJpeg(imOperation, message);
	}

	private IMOperation createIMOperationUsingInputPath(String inputPath) {
		IMOperation imOperation = imOperationFactory.factor();
		imOperation.addImage(inputPath);
		return imOperation;
	}

	private void tryToRunImageMagickJpeg(IMOperation imOperation, String message) {
		try {
			convertCmd.run(imOperation);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw BinaryConverterException.withMessageAndException(message, e);
		} catch (Exception e) {
			throw BinaryConverterException.withMessageAndException(message, e);
		}
	}

	private String createErrorMessageConvertAndResizeToJpeg(String inputPath, int width) {
		String errorMsg = "Error converting image on path {0} and width {1}";
		return MessageFormat.format(errorMsg, inputPath, width);
	}

	@Override
	public void convertToTiff(String inputPath, String outputPath) {
		IMOperation imOperation = createIMOperationUsingInputPath(inputPath);
		imOperation.addImage("TIFF:" + outputPath);

		String message = createErrorMessageConvertToTiff(inputPath);
		tryToRunImageMagickJpeg(imOperation, message);
	}

	private String createErrorMessageConvertToTiff(String inputPath) {
		String errorMsg = "Error converting image to TIFF on path {0}";
		return MessageFormat.format(errorMsg, inputPath);
	}

	public IMOperationFactory onlyForTestGetImOperationFactory() {
		return imOperationFactory;
	}

	public ConvertCmd onlyForTestGetConvertCmd() {
		return convertCmd;
	}
}
