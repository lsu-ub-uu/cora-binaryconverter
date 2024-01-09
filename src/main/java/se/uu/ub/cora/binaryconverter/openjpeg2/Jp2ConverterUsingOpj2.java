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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Command;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;

public class Jp2ConverterUsingOpj2 implements Jp2Converter {

	private Opj2Command opj2Command;
	private Opj2Parameters opj2Parameters;
	private ImageConverter imageConverter;

	public Jp2ConverterUsingOpj2(Opj2Command opj2Command, Opj2Parameters opj2Parameters,
			ImageConverter converterToTiff) {
		this.opj2Command = opj2Command;
		this.opj2Parameters = opj2Parameters;
		this.imageConverter = converterToTiff;
	}

	@Override
	public void convert(String inputPath, String outputPath, String mimeType) {

		/**
		 * TODO:
		 * 
		 * 1 Handle exceptions on Files operations.
		 * 
		 * When viewer is on place
		 * <p>
		 * 1. calculate numOfResolutions based on reslotuion
		 */

		Path tempFilePath = createTempFileForConvertion(inputPath, mimeType);
		convertToJp2UsingOpenJpeg(tempFilePath, outputPath);
		deleteTempFileForConvertion(tempFilePath);

	}

	private Path createTempFileForConvertion(String inputPath, String mimeType) {
		if (Opj2MimeType.isAcceptedForOpenJpeg2(mimeType)) {
			return createSymbolicLinkForInputPath(inputPath, mimeType);
		}
		return convertInputPathToTempTif(inputPath);
	}

	private Path createSymbolicLinkForInputPath(String inputPath, String mimeType) {
		String extension = Opj2MimeType.getExtensionForMimeType(mimeType);
		String inputPathWithExtension = inputPath + extension;

		Path inputPathAsPath = Paths.get(inputPath);
		Path symbolicLinkPath = Paths.get(inputPathWithExtension);
		tryToCreateSymbolicLink(inputPath, inputPathAsPath, symbolicLinkPath);
		return symbolicLinkPath;
	}

	private void tryToCreateSymbolicLink(String inputPath, Path inputPathAsPath,
			Path symbolicLinkPath) {
		try {
			Files.createSymbolicLink(symbolicLinkPath, inputPathAsPath);
		} catch (IOException e) {
			String errorMessage = "Error converting to OpenJpg2, could not create symbolic link for file {0}";
			throw BinaryConverterException
					.withMessageAndException(MessageFormat.format(errorMessage, inputPath), e);
		}
	}

	private Path convertInputPathToTempTif(String inputPath) {
		Path tempTif = Paths.get("/tmp/" + System.currentTimeMillis() + ".tif");
		imageConverter.convertToTiff(inputPath, tempTif.toString());
		return tempTif;
	}

	private void convertToJp2UsingOpenJpeg(Path tempFilePath, String outputPath) {
		opj2Parameters = setOpenJpeg2Settings(tempFilePath, outputPath);
		opj2Command.compress(opj2Parameters);
		removeJp2ExtensionFromOutputFileNameFromOpenJpeg2(outputPath);

	}

	private void removeJp2ExtensionFromOutputFileNameFromOpenJpeg2(String outputPath) {
		Path outPutPathWithJp2 = Paths.get(outputPath + ".jp2");
		Path outPutPathWithoutExtension = Paths.get(outputPath);
		try {
			Files.move(outPutPathWithJp2, outPutPathWithoutExtension,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Opj2Parameters setOpenJpeg2Settings(Path tempFilePath, String outputPath) {
		setStandardConvertionParameters();
		opj2Parameters.inputPath(tempFilePath.toString());
		opj2Parameters.outputPath(outputPath + ".jp2");
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

	private void deleteTempFileForConvertion(Path path) {

		try {
			Files.delete(path);
		} catch (Exception e) {
			// TODO: handle exception
		}
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
}
