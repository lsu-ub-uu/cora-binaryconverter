/*
 * Copyright 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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
package se.uu.ub.cora.binaryconverter.spy;

import se.uu.ub.cora.binaryconverter.document.PdfConverter;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class BinaryOperationFactorySpy implements BinaryOperationFactory {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public BinaryOperationFactorySpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factorImageAnalyzer", ImageAnalyzerSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorImageConverter", ImageConverterSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorPdfConverter", PdfConverterSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorJp2Converter", Jp2ConverterSpy::new);
	}

	@Override
	public ImageAnalyzer factorImageAnalyzer(String path) {
		return (ImageAnalyzer) MCR.addCallAndReturnFromMRV("path", path);
	}

	@Override
	public ImageConverter factorImageConverter() {
		return (ImageConverter) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public PdfConverter factorPdfConverter() {
		return (PdfConverter) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public Jp2Converter factorJp2Converter() {
		return (Jp2Converter) MCR.addCallAndReturnFromMRV();
	}
}
