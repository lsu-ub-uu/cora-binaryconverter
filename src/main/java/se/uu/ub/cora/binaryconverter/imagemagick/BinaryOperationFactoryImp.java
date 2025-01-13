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
package se.uu.ub.cora.binaryconverter.imagemagick;

import org.im4java.core.ConvertCmd;

import se.uu.ub.cora.binaryconverter.document.PdfConverter;
import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.imagemagick.document.PdfConverterImp;
import se.uu.ub.cora.binaryconverter.imagemagick.image.ImageAnalyzerImp;
import se.uu.ub.cora.binaryconverter.imagemagick.image.ImageConverterImp;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.openjpeg.FilesWrapper;
import se.uu.ub.cora.binaryconverter.openjpeg.FilesWrapperImp;
import se.uu.ub.cora.binaryconverter.openjpeg.Jp2ConverterUsingOpj;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjCommand;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjCommandImp;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjParameters;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjParametersImp;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjProcessRunnerFactory;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjProcessRunnerFactoryImp;

public class BinaryOperationFactoryImp implements BinaryOperationFactory {

	@Override
	public ImageAnalyzerImp factorImageAnalyzer(String someTempPath) {
		return new ImageAnalyzerImp(someTempPath);
	}

	@Override
	public ImageConverter factorImageConverter() {
		IMOperationFactory factory = new IMOperationFactoryImp();
		ConvertCmd command = new ConvertCmd();
		return new ImageConverterImp(factory, command);
	}

	@Override
	public PdfConverter factorPdfConverter() {
		IMOperationFactory factory = new IMOperationFactoryImp();
		ConvertCmd command = new ConvertCmd();
		return new PdfConverterImp(factory, command);
	}

	@Override
	public Jp2Converter factorJp2Converter() {
		OpjProcessRunnerFactory processRunnerFactory = new OpjProcessRunnerFactoryImp();
		OpjCommand command = new OpjCommandImp(processRunnerFactory);
		OpjParameters parameters = new OpjParametersImp();
		ImageConverter imageConverter = factorImageConverter();
		FilesWrapper filesWrapper = new FilesWrapperImp();

		return new Jp2ConverterUsingOpj(command, parameters, imageConverter, filesWrapper);
	}

}