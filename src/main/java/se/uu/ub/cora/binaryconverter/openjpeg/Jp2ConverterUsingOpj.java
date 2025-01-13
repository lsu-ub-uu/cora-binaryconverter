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
package se.uu.ub.cora.binaryconverter.openjpeg;

import java.text.MessageFormat;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjCommand;
import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjParameters;

public class Jp2ConverterUsingOpj implements Jp2Converter {

	private static final String TIF_EXTENSION = ".tif";
	private static final String JP2_EXTENSION = ".jp2";
	private OpjCommand opjCommand;
	private OpjParameters opjParameters;
	private ImageConverter imageConverter;
	private FilesWrapper filesWrapper;

	public Jp2ConverterUsingOpj(OpjCommand opjCommand, OpjParameters opjParameters,
			ImageConverter converterToTiff, FilesWrapper filesWrapper) {
		this.opjCommand = opjCommand;
		this.opjParameters = opjParameters;
		this.imageConverter = converterToTiff;
		this.filesWrapper = filesWrapper;
	}

	@Override
	public void convert(String inputPath, String outputPath, String mimeType) {
		tryToConvertToJp2(inputPath, outputPath, mimeType);
	}

	private void tryToConvertToJp2(String inputPath, String outputPath, String mimeType) {
		try {
			convertToJp2(inputPath, outputPath, mimeType);
		} catch (Exception e) {
			String errorMessage = "Error converting to jp2: {0}";
			throw BinaryConverterException
					.withMessageAndException(MessageFormat.format(errorMessage, e.getMessage()), e);
		}
	}

	private void convertToJp2(String inputPath, String outputPath, String mimeType) {
		String temporalyFile = createTempFileForConvertion(inputPath, mimeType);
		convertToJp2UsingOpenJpeg(temporalyFile, outputPath);
		filesWrapper.delete(temporalyFile);
	}

	private String createTempFileForConvertion(String inputPath, String mimeType) {
		if (OpjMimeType.isAcceptedForOpenJpeg2(mimeType)) {
			return createSymbolicLinkForInputPath(inputPath, mimeType);
		}
		return convertInputPathToTempTif(inputPath);
	}

	private String createSymbolicLinkForInputPath(String inputPath, String mimeType) {
		String extension = OpjMimeType.getExtensionForMimeType(mimeType);
		String symbolicLink = generateTempFileName(extension);

		filesWrapper.createSymbolicLink(symbolicLink, inputPath);

		return symbolicLink;
	}

	private String convertInputPathToTempTif(String inputPath) {
		String tempTif = generateTempFileName(TIF_EXTENSION);
		imageConverter.convertToTiff(inputPath, tempTif);
		return tempTif;
	}

	private String generateTempFileName(String extension) {
		return "/tmp/" + System.currentTimeMillis() + extension;
	}

	private void convertToJp2UsingOpenJpeg(String tempFile, String outputPath) {
		opjParameters = setOpenJpeg2Settings(tempFile, outputPath);
		opjCommand.compress(opjParameters);
		filesWrapper.move(outputPath + JP2_EXTENSION, outputPath);
	}

	private OpjParameters setOpenJpeg2Settings(String tempFile, String outputPath) {
		setStandardConvertionParameters();
		opjParameters.inputPath(tempFile);
		opjParameters.outputPath(outputPath + JP2_EXTENSION);
		return opjParameters;
	}

	private void setStandardConvertionParameters() {
		opjParameters.codeBlockSize(64, 64);
		opjParameters.precinctSize(256, 256);
		opjParameters.tileSize(1024, 1024);
		opjParameters.numOfResolutions(7);
		opjParameters.psnrQuality(60);
		opjParameters.progressionOrder("RPCL");
		opjParameters.enableEph();
		opjParameters.enableSop();
		opjParameters.enableTlm();
		opjParameters.enablePlt();
		opjParameters.tilePartDivider("R");
		opjParameters.numberOfThreads(6);
	}

	public OpjCommand onlyForTestGetOpjCommand() {
		return opjCommand;
	}

	public OpjParameters onlyForTestGetOpjParameters() {
		return opjParameters;
	}

	public ImageConverter onlyForTestGetImageConverter() {
		return imageConverter;
	}

	public FilesWrapper onlyForTestGetFilesWrapper() {
		return filesWrapper;
	}
}
