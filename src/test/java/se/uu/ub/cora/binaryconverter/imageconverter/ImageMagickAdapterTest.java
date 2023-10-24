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
package se.uu.ub.cora.binaryconverter.imageconverter;

import static org.testng.Assert.assertEquals;

import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.spy.ArrayListOutputConsumerSpy;
import se.uu.ub.cora.binaryconverter.imageconverter.spy.IMOperationSpy;
import se.uu.ub.cora.binaryconverter.imageconverter.spy.IdentifyCmdSpy;

public class ImageMagickAdapterTest {

	private static final String FORMAT_DPI_WIDTH_HEIGHT = "%x,%w,%h";
	private static final String SOME_TEMP_PATH = "/someTempPath";
	ImageMagickAdapter imageMagick;

	private IdentifyCmdSpy identifyCmd;
	private IMOperationSpy imOperation;
	private ArrayListOutputConsumerSpy outputConsumer;

	@BeforeMethod
	public void beforeMethod() {
		identifyCmd = new IdentifyCmdSpy();
		imOperation = new IMOperationSpy();
		outputConsumer = new ArrayListOutputConsumerSpy();

		imageMagick = new ImageMagickAdapaterImp(identifyCmd, imOperation, outputConsumer);
	}

	@Test
	public void testAnalyzeImage() throws Exception {

		ImageData imageData = imageMagick.analyze(SOME_TEMP_PATH);

		assertEquals(imageData.resolution(), "200");
		assertEquals(imageData.width(), "1920");
		assertEquals(imageData.height(), "1080");
	}

	@Test
	public void tesAnalyzeImageCallsImageMagick() throws Exception {

		imageMagick.analyze(SOME_TEMP_PATH);

		String[] pathAsArray = (String[]) imOperation.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("addImage", 0, "arg0");

		assertEquals(pathAsArray[0], SOME_TEMP_PATH);
		imOperation.MCR.assertParameters("format", 0, FORMAT_DPI_WIDTH_HEIGHT);

		identifyCmd.MCR.assertParameters("setOutputConsumer", 0, outputConsumer);
		identifyCmd.MCR.assertParameters("run", 0, imOperation);
		outputConsumer.MCR.methodWasCalled("getOutput");

	}

	@Test(enabled = false)
	public void testRealAnalyze() throws Exception {

		ImageMagickAdapaterImp imageMagickReal = new ImageMagickAdapaterImp(new IdentifyCmd(),
				new IMOperation(), new ArrayListOutputConsumer());

		imageMagickReal.analyze("/home/pere/workspace/gokuForever.jpg");

	}

}
