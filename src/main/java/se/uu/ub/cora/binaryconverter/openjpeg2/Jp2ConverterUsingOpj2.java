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
package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.text.MessageFormat;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Command;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;

public class Jp2ConverterUsingOpj2 implements Jp2Converter {

	private static final String TIF_EXTENSION = ".tif";
	private static final String JP2_EXTENSION = ".jp2";
	private Opj2Command opj2Command;
	private Opj2Parameters opj2Parameters;
	private ImageConverter imageConverter;
	private FilesWrapper filesWrapper;

	public Jp2ConverterUsingOpj2(Opj2Command opj2Command, Opj2Parameters opj2Parameters,
			ImageConverter converterToTiff, FilesWrapper filesWrapper) {
		this.opj2Command = opj2Command;
		this.opj2Parameters = opj2Parameters;
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
		if (Opj2MimeType.isAcceptedForOpenJpeg2(mimeType)) {
			return createSymbolicLinkForInputPath(inputPath, mimeType);
		}
		return convertInputPathToTempTif(inputPath);
	}

	private String createSymbolicLinkForInputPath(String inputPath, String mimeType) {
		String extension = Opj2MimeType.getExtensionForMimeType(mimeType);
		String symbolicLink = inputPath + extension;

		filesWrapper.createSymbolicLink(symbolicLink, inputPath);

		return symbolicLink;
	}

	private String convertInputPathToTempTif(String inputPath) {
		String tempTif = "/tmp/" + System.currentTimeMillis() + TIF_EXTENSION;

		imageConverter.convertToTiff(inputPath, tempTif);

		return tempTif;
	}

	private void convertToJp2UsingOpenJpeg(String tempFile, String outputPath) {
		opj2Parameters = setOpenJpeg2Settings(tempFile, outputPath);
		opj2Command.compress(opj2Parameters);
		filesWrapper.move(outputPath + JP2_EXTENSION, outputPath);
	}

	private Opj2Parameters setOpenJpeg2Settings(String tempFile, String outputPath) {
		setStandardConvertionParameters();
		opj2Parameters.inputPath(tempFile);
		opj2Parameters.outputPath(outputPath + JP2_EXTENSION);
		return opj2Parameters;
	}

	private void setStandardConvertionParameters() {
		opj2Parameters.codeBlockSize(64, 64);
		opj2Parameters.precinctSize(256, 256);
		opj2Parameters.tileSize(1024, 1024);
		opj2Parameters.numOfResolutions(7); // <-- Value is variable depending on resolution
		opj2Parameters.psnrQuality(25, 28, 30, 35, 40); // <-- tweak when viewer is in place
		opj2Parameters.progressionOrder("RPCL");
		opj2Parameters.enableEph();
		opj2Parameters.enableSop();
		opj2Parameters.enableTlm();
		opj2Parameters.enablePlt();
		opj2Parameters.tilePartDivider("R");
		opj2Parameters.numberOfThreads(6); // Runtime.getRuntime().availableProcessors() / 2;
	}

	public Opj2Command onlyForTestGetOpj2Command() {
		return opj2Command;
	}

	public Opj2Parameters onlyForTestGetOpj2Parameters() {
		return opj2Parameters;
	}

	public ImageConverter onlyForTestGetImageConverter() {
		return imageConverter;
	}

	public FilesWrapper onlyForTestGetFilesWrapper() {
		return filesWrapper;
	}
}
