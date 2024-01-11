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
package se.uu.ub.cora.binaryconverter.openjpeg2.spy;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessRunner;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2ProcessRunnerFactory;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class Opj2ProcessRunnerFactorySpy implements Opj2ProcessRunnerFactory {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public Opj2ProcessRunnerFactorySpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factor", Opj2ProcessRunnerSpy::new);
	}

	@Override
	public Opj2ProcessRunner factor(Opj2Parameters parameters) {
		return (Opj2ProcessRunner) MCR.addCallAndReturnFromMRV("parameters", parameters);
	}

}
