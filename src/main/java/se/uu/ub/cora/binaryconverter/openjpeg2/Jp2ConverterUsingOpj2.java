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

import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Command;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;

public class Jp2ConverterUsingOpj2 implements Jp2Converter {

	private Opj2Command opj2Command;
	private Opj2Parameters opj2Parameters;

	public Jp2ConverterUsingOpj2(Opj2Command opj2Command, Opj2Parameters opj2Parameters) {
		this.opj2Command = opj2Command;
		this.opj2Parameters = opj2Parameters;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void convert(String inputPath, String outputPath) {

		/**
		 * TODO:
		 * 
		 * 1. check media type, if different than .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw, .tga,
		 * .tif then convert to tiff first and continue to convert with openjp2
		 * <p>
		 * 2. calculate numOfResolutions based on reslotuion
		 * 
		 */

		setOpenJpeg2Settings(inputPath, outputPath);
		opj2Command.compress(opj2Parameters);
	}

	private void setOpenJpeg2Settings(String inputPath, String outputPath) {
		opj2Parameters.inputPath(inputPath);
		opj2Parameters.outputPath(outputPath);
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

	// TODO
	// Valid input image extensions are .bmp, .pgm, .pgx, .png, .pnm, .ppm, .raw, .tga, .tif<br>
}
