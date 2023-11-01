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

import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.messaging.MessageListener;

public class AnalyzeAndConvertStarterFactoryImp implements AnalyzeAndConvertStarterFactory {

	@Override
	public AnalyzeAndConvertStarter factor(CoraClientFactory coraClientFactory,
			MessageListener messageListener, String someUserId, String someApptoken,
			String someOcflHome) {
		return new AnalyzeAndConvertStarterImp(coraClientFactory, messageListener, someUserId,
				someApptoken, someOcflHome);
	}
}