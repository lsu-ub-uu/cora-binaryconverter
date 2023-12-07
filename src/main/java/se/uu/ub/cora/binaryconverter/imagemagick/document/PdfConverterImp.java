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
package se.uu.ub.cora.binaryconverter.imagemagick.document;

import java.text.MessageFormat;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import se.uu.ub.cora.binaryconverter.document.PdfConverter;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

public class PdfConverterImp implements PdfConverter {

	private ConvertCmd convertCmd;
	private IMOperationFactory imOperationFactory;
	private static final double QUALITY = 90.0;
	private static final String OUTPUT_FORMAT = "JPG:";

	public PdfConverterImp(IMOperationFactory imOperationFactory, ConvertCmd convertCmd) {
		this.imOperationFactory = imOperationFactory;
		this.convertCmd = convertCmd;
	}

	@Override
	public void convertUsingWidth(String inputPath, String outputPath, int width) {
		IMOperation imOperation = createImOperationForPdfConverter(inputPath, outputPath, width);
		try {
			convertCmd.run(imOperation);
		} catch (Exception e) {
			String errorMsg = "Error creating first page thumbnail of a PDF on path {0} and width {1}";
			String message = MessageFormat.format(errorMsg, inputPath, width);
			throw BinaryConverterException.withMessageAndException(message, e);
		}
	}

	private IMOperation createImOperationForPdfConverter(String inputPath, String outputPath,
			int width) {
		IMOperation imOperation = imOperationFactory.factor();
		imOperation.addImage(inputPath + "[0]");
		imOperation.resize(width);
		imOperation.quality(QUALITY);
		imOperation.alpha("remove");
		imOperation.addImage(OUTPUT_FORMAT + outputPath);
		return imOperation;
	}

	public IMOperationFactory onlyForTestGetImOperationFactory() {
		return imOperationFactory;
	}

	public ConvertCmd onlyForTestGetConvertCmd() {
		return convertCmd;
	}
}
