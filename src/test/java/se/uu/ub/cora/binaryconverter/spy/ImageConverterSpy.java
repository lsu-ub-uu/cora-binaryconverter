/*
 * Copyright 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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
package se.uu.ub.cora.binaryconverter.spy;

import static org.testng.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ImageConverterSpy implements ImageConverter {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public ImageConverterSpy() {
		MCR.useMRV(MRV);
	}

	@Override
	public void convertAndResizeUsingWidth(String inputPath, String outputPath, int width) {
		MCR.addCall("inputPath", inputPath, "outputPath", outputPath, "width", width);
	}

	@Override
	public void convertToTiff(String inputPath, String outputPath) {
		MCR.addCall("inputPath", inputPath, "outputPath", outputPath);

		Path path = Paths.get(outputPath);

		try {
			Files.createFile(path);
		} catch (IOException e) {
			fail("It could not create file: " + outputPath);
		}
	}
}
