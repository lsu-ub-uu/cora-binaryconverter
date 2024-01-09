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
package se.uu.ub.cora.binaryconverter.openjpeg2;

import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.imagemagick.BinaryOperationFactoryImp;

public class Jp2ConverterUsingOpj2RealTest {

	@Test(enabled = false)
	public void realTest2() {
		// TODO Auto-generated method stub
		BinaryOperationFactoryImp factory = new BinaryOperationFactoryImp();
		Jp2Converter jp2Converter = factory.factorJp2Converter();
		jp2Converter.convert(
				"/tmp/sharedFileStorage/systemOne/streams/systemOne/binary:binary:1296461930134329-large",
				"/tmp/sharedFileStorage/systemOne/streams/systemOne/binary:binary:1296461930134329-jp2",
				"image/jpeg");
		// "/tmp/sharedFileStorage/systemOne/streams/systemOne/binary:binary:1296461930134329-jp2",
	}
}
