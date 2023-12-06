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
package se.uu.ub.cora.binaryconverter.imagemagick;

import org.im4java.core.ConvertCmd;

import se.uu.ub.cora.binaryconverter.document.Jp2ConverterFactory;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.imagemagick.image.Jp2ConverterImp;

public class Jp2ConverterFactoryImp implements Jp2ConverterFactory {

	@Override
	public Jp2Converter factor() {
		IMOperationFactory imOperationFactory = new IMOperationFactoryImp();
		ConvertCmd command = new ConvertCmd();
		return new Jp2ConverterImp(imOperationFactory, command);
	}

}
