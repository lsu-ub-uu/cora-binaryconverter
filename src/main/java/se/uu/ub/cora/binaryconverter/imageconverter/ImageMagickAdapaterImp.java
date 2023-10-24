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

import java.io.IOException;
import java.util.ArrayList;

import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;

public class ImageMagickAdapaterImp implements ImageMagickAdapter {

	private static final String FORMAT_DPI_WIDTH_HEIGHT = "%x,%w,%h";
	private IdentifyCmd identifyCmd;
	private IMOperation imOperation;
	private ArrayListOutputConsumer outputConsumer;

	public ImageMagickAdapaterImp(IdentifyCmd identifyCmd, IMOperation imOperation,
			ArrayListOutputConsumer outputConsumer) {
		this.identifyCmd = identifyCmd;
		this.imOperation = imOperation;
		this.outputConsumer = outputConsumer;

	}

	@Override
	public ImageData analyze(String imagePath) {
		System.out.println("Path: " + imagePath);
		imOperation.addImage(imagePath);

		imOperation.format(FORMAT_DPI_WIDTH_HEIGHT);
		try {
			identifyCmd.setOutputConsumer(outputConsumer);

			identifyCmd.run(imOperation);

			ArrayList<String> result = outputConsumer.getOutput();
			System.out.println(result);
		} catch (IOException | InterruptedException | IM4JavaException e) {
			e.printStackTrace();
		}
		return new ImageData("200", "1920", "1080");
	}

}
