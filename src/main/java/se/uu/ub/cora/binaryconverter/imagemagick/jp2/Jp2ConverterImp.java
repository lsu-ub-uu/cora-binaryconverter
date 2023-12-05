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
package se.uu.ub.cora.binaryconverter.imagemagick.jp2;

import java.text.MessageFormat;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.ArrayListOutputConsumer;

import se.uu.ub.cora.binaryconverter.common.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.imagemagick.IMOperationFactory;

public class Jp2ConverterImp implements Jp2Converter {
	private IMOperationFactory imOperationFactory;
	private ConvertCmd convertCmd;

	ArrayListOutputConsumer outputConsumer = new ArrayListOutputConsumer();

	public Jp2ConverterImp(IMOperationFactory imOperationFactory, ConvertCmd convertCmd) {
		this.imOperationFactory = imOperationFactory;
		this.convertCmd = convertCmd;
	}

	@Override
	public void convert(String inputPath, String outputPath) {
		IMOperation imOperation = imOperationFactory.factor();
		imOperation.addImage(inputPath);
		addJpeg2000Defintions(imOperation);
		imOperation.addImage("JP2:" + outputPath);
		try {
			convertCmd.run(imOperation);
		} catch (Exception e) {
			String errorMsg = "Error converting to Jpeg2000 image on path {0}";
			String message = MessageFormat.format(errorMsg, inputPath);
			throw BinaryConverterException.withMessageAndException(message, e);
		}
	}

	/*
	 * These settings are based upon the recommended optmized settings from IIPImage
	 */
	private void addJpeg2000Defintions(IMOperation imOperation) {
		imOperation.define("jp2:progression-order=RPCL");
		imOperation.define("jp2:quality=25,28,30,35,40");
		imOperation.define("jp2:prcwidth=256");
		imOperation.define("jp2:prcheight=256");
		imOperation.define("jp2:cblkwidth=64");
		imOperation.define("jp2:cblkheight=64");
		imOperation.define("jp2:sop");
		imOperation.define("jp2:eph");
	}

	void onlyForTestSetConvertCmd(ConvertCmd convertCmd) {
		this.convertCmd = convertCmd;
	}

	public IMOperationFactory onlyForTestGetImOperationFactory() {
		return imOperationFactory;
	}

	public ConvertCmd onlyForTestGetConvertCmd() {
		return convertCmd;
	}
}
