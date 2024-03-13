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
package se.uu.ub.cora.binaryconverter.openjpeg2.adapter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Opj2ParametersTest {

	Opj2Parameters params;

	@BeforeMethod
	public void beforeMethod() {
		params = new Opj2ParametersImp();
	}

	@Test
	public void testInputPath() throws Exception {
		String path = "/some/input/path";
		params.inputPath(path);
		Assert.assertEquals(params.getInputPath(), path);
		assertOpsPresentInParamListAndInOrder("-i", path);
	}

	private void assertOpsPresentInParamListAndInOrder(String param, String value) {
		List<String> paramsList = params.getParamsList();
		int paramIndex = paramsList.indexOf(param);
		assertEquals(paramsList.get(paramIndex + 1), value);
	}

	@Test
	public void testOutputPath() throws Exception {
		String path = "/some/output/Path";
		params.outputPath(path);
		Assert.assertEquals(params.getOutputPath(), path);
		assertOpsPresentInParamListAndInOrder("-o", path);
	}

	@Test
	public void testPsnrQuality() throws Exception {
		params.psnrQuality(10, 20, 30, 40, 50);
		assertOpsPresentInParamListAndInOrder("-q", "10,20,30,40,50");
	}

	@Test
	public void testPsnrQualityOnlyOneValue() throws Exception {
		params.psnrQuality(50);
		assertOpsPresentInParamListAndInOrder("-q", "50");
	}

	@Test
	public void testTileSize() throws Exception {
		params.tileSize(1024, 1024);
		assertOpsPresentInParamListAndInOrder("-t", "1024,1024");
	}

	@Test
	public void testCompressionRatio() throws Exception {
		params.compressionRatio(50, 40, 30, 20, 10);
		assertOpsPresentInParamListAndInOrder("-r", "50,40,30,20,10");
	}

	@Test
	public void testCompressionRatioOnlyOneValue() throws Exception {
		params.compressionRatio(50);
		assertOpsPresentInParamListAndInOrder("-r", "50");
	}

	@Test
	public void testResolutions() throws Exception {
		params.numOfResolutions(7);
		assertOpsPresentInParamListAndInOrder("-n", "7");
	}

	// @Test
	// public void testPrecinctSizeNoValueIsRepeatedToCreateAPair() throws Exception {
	// params.precinctSize();
	// assertOpsPresentInParamListAndInOrder("-c", "");
	// }

	@Test
	public void testPrecinctSizeOnlyOneValueIsRepeatedToCreateAPair() throws Exception {
		params.precinctSize(64);
		assertOpsPresentInParamListAndInOrder("-c", "[64,64]");
	}

	@Test
	public void testPrecinctSize() throws Exception {
		params.precinctSize(256, 128);
		assertOpsPresentInParamListAndInOrder("-c", "[256,128]");
	}

	@Test
	public void testMultiplePrecinctSizesWithUnevenAmountOfValues() throws Exception {
		params.precinctSize(256, 256, 128);
		assertOpsPresentInParamListAndInOrder("-c", "[256,256],[128,128]");
	}

	@Test
	public void testCodeBlockSize() throws Exception {
		params.codeBlockSize(64, 64);
		assertOpsPresentInParamListAndInOrder("-b", "64,64");
	}

	@Test
	public void testProgressionOrder() throws Exception {
		params.progressionOrder("RCLP");
		assertOpsPresentInParamListAndInOrder("-p", "RCLP");
	}

	@Test
	public void testEnableSop() throws Exception {
		params.enableSop();
		assertTrue(params.getParamsList().contains("-SOP"));
	}

	@Test
	public void testEnableEph() throws Exception {
		params.enableEph();
		assertTrue(params.getParamsList().contains("-EPH"));
	}

	@Test
	public void testEnablePlt() throws Exception {
		params.enablePlt();
		assertTrue(params.getParamsList().contains("-PLT"));
	}

	@Test
	public void testEnableTlm() throws Exception {
		params.enableTlm();
		assertTrue(params.getParamsList().contains("-TLM"));
	}

	@Test
	public void testNumberOfThreads() throws Exception {
		params.numberOfThreads(4);
		assertOpsPresentInParamListAndInOrder("-threads", "4");
	}

	@Test
	public void testNumberOfThreadsUseAllCores() throws Exception {
		params.numberOfThreads(0);
		assertOpsPresentInParamListAndInOrder("-threads", "ALL_CPUS");
	}

	@Test
	public void testTilePartDivider() throws Exception {
		params.tilePartDivider("r");
		assertOpsPresentInParamListAndInOrder("-TP", "R");
	}

	@Test
	public void testMultipleCommands() throws Exception {
		params.inputPath("/input/path");
		params.outputPath("/output/path");
		params.enableEph();
		params.codeBlockSize(64, 64);
		params.enableSop();
		params.precinctSize(256, 256);
		params.enableTlm();
		params.numOfResolutions(6);
		params.enablePlt();
		params.psnrQuality(10, 20, 30);
		params.progressionOrder("RPCL");
		params.tilePartDivider("C");
		params.numberOfThreads(8);

		assertOpsPresentInParamListAndInOrder("-i", "/input/path");
		assertOpsPresentInParamListAndInOrder("-o", "/output/path");
		assertTrue(params.getParamsList().contains("-EPH"));
		assertOpsPresentInParamListAndInOrder("-b", "64,64");
		assertTrue(params.getParamsList().contains("-SOP"));
		assertOpsPresentInParamListAndInOrder("-c", "[256,256]");
		assertTrue(params.getParamsList().contains("-TLM"));
		assertOpsPresentInParamListAndInOrder("-n", "6");
		assertTrue(params.getParamsList().contains("-PLT"));
		assertOpsPresentInParamListAndInOrder("-q", "10,20,30");
		assertOpsPresentInParamListAndInOrder("-p", "RPCL");
		assertOpsPresentInParamListAndInOrder("-threads", "8");
		assertOpsPresentInParamListAndInOrder("-TP", "C");
	}

	@Test
	public void testAddFirst() throws Exception {
		params.inputPath("/input/path");
		params.outputPath("/output/path");

		assertEquals(params.getParamsList().size(), 4);

		params.opj2Command("someCommand");
		assertEquals(params.getParamsList().size(), 5);
		assertEquals(params.getParamsList().get(0), "someCommand");
	}
}
