package se.uu.ub.cora.binaryconverter.openjpeg2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Opj2OpsTest {

	Opj2Ops ops;

	@BeforeMethod
	public void beforeMethod() {
		ops = new Opj2OpsImp();
	}

	@Test
	public void testInputPath() throws Exception {
		String path = "/some/input/path";
		ops.inputPath(path);
		Assert.assertEquals(ops.getInputPath(), path);
		assertOpsPresentInOpsListAndInOrder("-i", path);
	}

	@Test
	public void testOutputPath() throws Exception {
		String path = "/some/output/Path";
		ops.outputPath(path);
		Assert.assertEquals(ops.getOutputPath(), path);
		assertOpsPresentInOpsListAndInOrder("-o", path);
	}

	@Test
	public void testPsnrQuality() throws Exception {
		ops.psnrQuality(10, 20, 30, 40, 50);
		assertOpsPresentInOpsListAndInOrder("-q", "10,20,30,40,50");
	}

	@Test
	public void testPsnrQualityOnlyOneValue() throws Exception {
		ops.psnrQuality(50);
		assertOpsPresentInOpsListAndInOrder("-q", "50");
	}

	@Test
	public void testTileSize() throws Exception {
		ops.tileSize(1024, 1024);
		assertOpsPresentInOpsListAndInOrder("-t", "1024,1024");
	}

	@Test
	public void testCompressionRatio() throws Exception {
		ops.compressionRatio(50, 40, 30, 20, 10);
		assertOpsPresentInOpsListAndInOrder("-r", "50,40,30,20,10");
	}

	@Test
	public void testCompressionRatioOnlyOneValue() throws Exception {
		ops.compressionRatio(50);
		assertOpsPresentInOpsListAndInOrder("-r", "50");
	}

	@Test
	public void testResolutions() throws Exception {
		ops.numOfResolutions(7);
		assertOpsPresentInOpsListAndInOrder("-n", "7");
	}

	@Test
	public void testPrecinctSizeOnlyOneValueIsRepeatedToCreateAPair() throws Exception {
		ops.precinctSize(64);
		assertOpsPresentInOpsListAndInOrder("-c", "[64,64]");
	}

	@Test
	public void testPrecinctSize() throws Exception {
		ops.precinctSize(256, 256);
		assertOpsPresentInOpsListAndInOrder("-c", "[256,256]");
	}

	@Test
	public void testMultiplePrecinctSizesWithUnevenAmountOfValues() throws Exception {
		ops.precinctSize(256, 256, 128);
		assertOpsPresentInOpsListAndInOrder("-c", "[256,256],[128,128]");
	}

	@Test
	public void testCodeBlockSize() throws Exception {
		ops.codeBlockSize(64, 64);
		assertOpsPresentInOpsListAndInOrder("-b", "64,64");
	}

	@Test
	public void testProgressionOrder() throws Exception {
		ops.progressionOrder("RCLP");
		assertOpsPresentInOpsListAndInOrder("-p", "RCLP");
	}

	@Test
	public void testEnableSop() throws Exception {
		ops.enableSop();
		assertTrue(ops.getOpsList().contains("-SOP"));
	}

	@Test
	public void testEnableEph() throws Exception {
		ops.enableEph();
		assertTrue(ops.getOpsList().contains("-EPH"));
	}

	@Test
	public void testEnablePlt() throws Exception {
		ops.enablePlt();
		assertTrue(ops.getOpsList().contains("-PLT"));
	}

	@Test
	public void testEnableTlm() throws Exception {
		ops.enableTlm();
		assertTrue(ops.getOpsList().contains("-TLM"));
	}

	@Test
	public void testNumberOfThreads() throws Exception {
		ops.numberOfThreads(4);
		assertOpsPresentInOpsListAndInOrder("-threads", "4");
	}

	@Test
	public void testTilePartDivider() throws Exception {
		ops.tilePartDivider("r");
		assertOpsPresentInOpsListAndInOrder("-TP", "R");
	}

	@Test
	public void testMultipleCommands() throws Exception {
		ops.inputPath("/input/path");
		ops.outputPath("/output/path");
		ops.enableEph();
		ops.codeBlockSize(64, 64);
		ops.enableSop();
		ops.precinctSize(256, 256);
		ops.enableTlm();
		ops.numOfResolutions(6);
		ops.enablePlt();
		ops.psnrQuality(10, 20, 30);
		ops.progressionOrder("RPCL");
		ops.tilePartDivider("C");
		ops.numberOfThreads(8);

		assertOpsPresentInOpsListAndInOrder("-i", "/input/path");
		assertOpsPresentInOpsListAndInOrder("-o", "/output/path");
		assertTrue(ops.getOpsList().contains("-EPH"));
		assertOpsPresentInOpsListAndInOrder("-b", "64,64");
		assertTrue(ops.getOpsList().contains("-SOP"));
		assertOpsPresentInOpsListAndInOrder("-c", "[256,256]");
		assertTrue(ops.getOpsList().contains("-TLM"));
		assertOpsPresentInOpsListAndInOrder("-n", "6");
		assertTrue(ops.getOpsList().contains("-PLT"));
		assertOpsPresentInOpsListAndInOrder("-q", "10,20,30");
		assertOpsPresentInOpsListAndInOrder("-p", "RPCL");
		assertOpsPresentInOpsListAndInOrder("-threads", "8");
		assertOpsPresentInOpsListAndInOrder("-TP", "C");
	}

	private void assertOpsPresentInOpsListAndInOrder(String command, String value) {
		List<String> opsList = ops.getOpsList();
		int commandIndex = opsList.indexOf(command);
		assertEquals(opsList.get(commandIndex + 1), value);
	}
}
