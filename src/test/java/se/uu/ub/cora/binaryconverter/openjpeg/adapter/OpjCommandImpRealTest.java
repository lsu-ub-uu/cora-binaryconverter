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
package se.uu.ub.cora.binaryconverter.openjpeg.adapter;

import java.io.IOException;

import org.testng.annotations.Test;

public class OpjCommandImpRealTest {

	@Test(enabled = false)
	public void realTest() throws OpenJpegException, IOException {
		// Settings lifted from iipimage (except -q that is from earlier alvin implementation):
		// opj_compress -i input.tif -o output.jp2 -q 25,28,30,35,46 (-r 2.5) -n 7 -c
		// "[256,256]" -b "64,64" -p RPCL -SOP -PLT -TLM -TP R
		//
		// "It is also possible to use tiling within JPEG2000, but the existence of precincts and
		// code blocks largely replicates the features of the tiled pyramid structure used by TIFF
		// making JPEG2000 tiling unnecessary and often even counter-productive for fast random
		// access. Unlike with tiled pyramid TIFF, tiling with JPEG2000 is only applied to the full
		// size image and not the sub-resolutions. Accessing a lower resolution, therefore, requires
		// accessing every tile, which will make decoding speed slower. Moreover, it is not
		// recommended to use tiling when using lossy JPEG2000 compression as compression artifacts
		// can be visible at tile boundaries (compression artifacts are not an issue with lossless
		// encoding)."
		// If tiling is required -t 1024,1024
		// note; dnf install openjpeg2-tools

		OpjParameters opjParams = new OpjParametersImp();
		opjParams.inputPath(
				"/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aTiff.tiff");
		opjParams.outputPath(
				"/home/pere/workspace/cora-fitnesse/FitNesseRoot/files/testResources/opjiipView.jp2");
		opjParams.codeBlockSize(64, 64);
		opjParams.precinctSize(256, 256);
		opjParams.tileSize(1024, 1024);
		opjParams.numOfResolutions(7);
		opjParams.psnrQuality(25, 28, 30, 35, 46);
		opjParams.progressionOrder("RPCL");
		opjParams.enableEph();
		opjParams.enableSop();
		opjParams.enableTlm();
		opjParams.enablePlt();
		opjParams.tilePartDivider("R");
		opjParams.numberOfThreads(6);

		// opjOps.compressionRatio(2);

		OpjProcessRunnerFactory factory = new OpjProcessRunnerFactoryImp();
		OpjCommand opjCompress = new OpjCommandImp(factory);
		opjCompress.compress(opjParams);
	}
}
