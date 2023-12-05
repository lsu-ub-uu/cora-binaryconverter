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
import org.im4java.process.ArrayListOutputConsumer;

import se.uu.ub.cora.binaryconverter.common.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactory;

public class ImageConverterImp implements ImageConverter {
	private static final double QUALITY = 90.0;
	private IMOperationFactory imOperationFactory;
	private ConvertCmd convertCmd;

	ArrayListOutputConsumer outputConsumer = new ArrayListOutputConsumer();

	public ImageConverterImp(IMOperationFactory imOperationFactory, ConvertCmd convertCmd) {
		this.imOperationFactory = imOperationFactory;
		this.convertCmd = convertCmd;
	}

	@Override
	public void convertUsingWidth(String inputPath, String outputPath, int width) {
		IMOperation imOperation = imOperationFactory.factor();
		imOperation.addImage(inputPath);
		imOperation.resize(width);
		imOperation.quality(QUALITY);
		imOperation.addImage("JPEG:" + outputPath);
		try {
			convertCmd.run(imOperation);
		} catch (Exception e) {
			String errorMsg = "Error converting image on path {0} and width {1}";
			String message = MessageFormat.format(errorMsg, inputPath, width);
			throw BinaryConverterException.withMessageAndException(message, e);
		}
	}

	void onlyForTestSetConvertCmd(ConvertCmd convertCmd) {
		this.convertCmd = convertCmd;

	}

	public IMOperationFactory onlyForTestGetImOperationFactory() {
		return imOperationFactory;
	}

	public ConvertCmd onlyForTestGetConvertCmd() {
		return convertCmd;
	}
}
