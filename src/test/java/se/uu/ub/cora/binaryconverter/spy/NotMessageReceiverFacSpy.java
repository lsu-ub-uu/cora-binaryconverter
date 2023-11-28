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

import se.uu.ub.cora.binaryconverter.imageconverter.MessageReceiverFactory;
import se.uu.ub.cora.binaryconverter.imageconverter.NotMessageReceiverFac;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class NotMessageReceiverFacSpy implements NotMessageReceiverFac {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public NotMessageReceiverFacSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factor", MessageReceiverFactorySpy::new);
	}

	@Override
	public MessageReceiverFactory factor(MessageListener messageListener,
			JavaClientAppTokenCredentials appTokenCredentials, String ocflHome,
			String fileStorageBasePath) {
		return (MessageReceiverFactory) MCR.addCallAndReturnFromMRV("messageListener",
				messageListener, "appTokenCredentials", appTokenCredentials, "ocflHome", ocflHome,
				"fileStorageBasePath", fileStorageBasePath);
	}

}
