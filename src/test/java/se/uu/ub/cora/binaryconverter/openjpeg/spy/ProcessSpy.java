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
package se.uu.ub.cora.binaryconverter.openjpeg.spy;

import java.io.InputStream;
import java.io.OutputStream;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class ProcessSpy extends Process {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public ProcessSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("waitFor", () -> 0);
		MRV.setDefaultReturnValuesSupplier("exitValue", () -> 0);
		MRV.setDefaultReturnValuesSupplier("isAlive", () -> true);
		MRV.setDefaultReturnValuesSupplier("destroyForcibly", ProcessSpy::new);
	}

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public int waitFor() throws InterruptedException {
		return (int) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public int exitValue() {
		return (int) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public void destroy() {
		MCR.addCall();

	}

	@Override
	public boolean isAlive() {
		return (boolean) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public Process destroyForcibly() {
		return (Process) MCR.addCallAndReturnFromMRV();
	}
}
