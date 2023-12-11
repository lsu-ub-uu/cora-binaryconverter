package se.uu.ub.cora.binaryconverter.openjpeg2;

import org.testng.annotations.Test;

public class Opj2CompressImpTest {

	@Test(enabled = false)
	public void test() {
		// opj2_compress -i input.tif -o output.jp2 -q 25,28,30,35,40 -t 1024,1024 -n 7 -c
		// "[256,256]" -b "64,64" -p RPCL -SOP -PLT -TLM -TP R

		Opj2Ops opj2Ops = new Opj2OpsImp();
		opj2Ops.inputPath(
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/sagradaFamilia.tiff");
		opj2Ops.outputPath(
				"/home/marcus/workspace/cora-fitnesse/FitNesseRoot/files/testResources/opj2output.jp2");
		opj2Ops.codeBlockSize("64,64");
		opj2Ops.precinctSize("256,256");
		opj2Ops.tileSize("1024,1024");
		opj2Ops.numOfResolutions(7);
		opj2Ops.psnrQuality("25,28,30,35,40");
		opj2Ops.progressionOrder("RPCL");
		opj2Ops.enableEph(true);
		opj2Ops.enableSop(true);

		Opj2Compress opj2Compress = new Opj2CompressImp();
		opj2Compress.run(opj2Ops);
	}
}
