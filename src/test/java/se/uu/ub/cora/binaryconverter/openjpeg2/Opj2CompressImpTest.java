package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.io.IOException;

import org.testng.annotations.Test;

public class Opj2CompressImpTest {

	@Test(enabled = true)
	public void realTest() throws OpenJpeg2Exception, InterruptedException, IOException {
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

		Opj2Ops opj2Ops = new Opj2OpsImp();
		opj2Ops.inputPath(
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/aTiff.tiff");
		opj2Ops.outputPath(
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/opj2iipView.jp2");
		opj2Ops.codeBlockSize(64, 64);
		opj2Ops.precinctSize(256, 256);
		opj2Ops.tileSize(1024, 1024);
		opj2Ops.numOfResolutions(7);
		opj2Ops.psnrQuality(25, 28, 30, 35, 46);
		opj2Ops.progressionOrder("RPCL");
		opj2Ops.enableEph();
		opj2Ops.enableSop();
		opj2Ops.enableTlm();
		opj2Ops.enablePlt();
		opj2Ops.tilePartDivider("R");
		opj2Ops.numberOfThreads(6);

		// opj2Ops.compressionRatio(2);

		Opj2Compress opj2Compress = new Opj2CompressImp();
		opj2Compress.run(opj2Ops);
	}
}
