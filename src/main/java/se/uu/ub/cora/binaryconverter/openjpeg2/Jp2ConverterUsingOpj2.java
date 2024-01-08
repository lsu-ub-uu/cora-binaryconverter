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
import java.util.List;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
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
		 * 1.OpenJpeg needs extensions on both input and output files. Input files should be fixed
		 * using a symbolic link when no temp convertion need it. and output file should be created
		 * with .jp2 and then moved to a file without extensions.
		 * 
		 * 1. Test symbolic link deleted.
		 * 
		 * 1. calculate numOfResolutions based on reslotuion
		 * <p>
		 * 2. Fail handling
		 * <p>
		 * 3. Handle exception if temp file cannot be deleted
		 * 
		 */
		if (mimeTypeOfImageNotAcceptedForOpenJpeg2(mimeType)) {
			String tempFilePath = "/tmp/" + System.currentTimeMillis() + ".tiff";
			imageConverter.convertToTiff(inputPath, tempFilePath);
			opj2Parameters = setOpenJpeg2Settings(tempFilePath, outputPath);
			opj2Command.compress(opj2Parameters);

			deleteTempFile(tempFilePath);
		} else {
			Path inputPathWithoutExtension = Paths.get(inputPath);
			Path inputPathWithBmp = Paths.get(inputPath + ".bmp");
			try {
				Files.createSymbolicLink(inputPathWithBmp, inputPathWithoutExtension);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			setOpenJpeg2Settings(inputPath + ".bmp", outputPath);
			opj2Command.compress(opj2Parameters);
		}
		moveOutPutPathWithoutExtension(outputPath);

	}

	private void moveOutPutPathWithoutExtension(String outputPath) {
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

	private boolean mimeTypeOfImageNotAcceptedForOpenJpeg2(String mimeType) {
		List<String> acceptMimeTypesForOpenJpeg2 = List.of("image/bmp", "image/x-portable-graymap",
				"image/png", "image/x-portable-anymap", "image/x-portable-pixmap",
				"image/x-raw-panasonic", "image/x-tga", "image/tiff");
		return !acceptMimeTypesForOpenJpeg2.contains(mimeType);
	}

	private Opj2Parameters setOpenJpeg2Settings(String inputPath, String outputPath) {
		opj2Parameters.inputPath(inputPath);
		opj2Parameters.outputPath(outputPath + ".jp2");
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
		return opj2Parameters;
	}

	private void deleteTempFile(String tempFilePath) {
		Path path = Paths.get(tempFilePath);

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
		// TODO Auto-generated method stub
		return imageConverter;
	}
}
