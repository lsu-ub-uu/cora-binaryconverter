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
package se.uu.ub.cora.binaryconverter.imageconverter.imagemagick.spy;

import java.io.IOException;

import org.im4java.core.IM4JavaException;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.Operation;
import org.im4java.process.OutputConsumer;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class IdentifyCmdSpy extends IdentifyCmd {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public IdentifyCmdSpy() {
		MCR.useMRV(MRV);
	}

	@Override
	public void run(Operation arg0, Object... arg1)
			throws IOException, InterruptedException, IM4JavaException {
		MCR.addCall("arg0", arg0, "arg1", arg1);
	}

	@Override
	public void setOutputConsumer(OutputConsumer arg0) {
		/**
		 * IdentifyCmd extends ImageCommand class which in its constructor calls setOutputConsumer.
		 * When this happens MCR is still null since IndentifyCmdSpy code has not been initialized
		 * yet. We MITIGATE the problem but initializing MCR in this method.
		 */
		if (MCR == null) {
			MCR = new MethodCallRecorder();
		}
		MCR.addCall("arg0", arg0);
	}

}
