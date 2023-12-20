package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;

import org.testng.annotations.Test;

public class Opj2CommandImpTest {

	@Test(enabled = true)
	public void realTest() throws OpenJpeg2Exception, IOException {
		// Settings lifted from iipimage (except -q that is from earlier alvin implementation):
		// opj2_compress -i input.tif -o output.jp2 -q 25,28,30,35,46 (-r 2.5) -n 7 -c
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

		Opj2Parameters opj2Params = new Opj2ParametersImp();
		opj2Params.inputPath(
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aTiff.tiff");
		opj2Params.outputPath(
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/opj2iipView.jp2");
		opj2Params.codeBlockSize(64, 64);
		opj2Params.precinctSize(256, 256);
		opj2Params.tileSize(1024, 1024);
		opj2Params.numOfResolutions(7);
		opj2Params.psnrQuality(25, 28, 30, 35, 46);
		opj2Params.progressionOrder("RPCL");
		opj2Params.enableEph();
		opj2Params.enableSop();
		opj2Params.enableTlm();
		opj2Params.enablePlt();
		opj2Params.tilePartDivider("R");
		opj2Params.numberOfThreads(6);

		// opj2Ops.compressionRatio(2);

		Opj2Command opj2Compress = new Opj2CommandImp();
		opj2Compress.compress(opj2Params);
	}
}
