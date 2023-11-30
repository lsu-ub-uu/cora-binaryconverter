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
package se.uu.ub.cora.binaryconverter.imagemagick.spy;

import java.util.ArrayList;
import java.util.Arrays;

import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;
import org.im4java.core.Operation;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class IMOperationSpy extends IMOperation {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();
	public ArrayList<String> callsInOrder = new ArrayList<>();

	public IMOperationSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("addImage", OperationSpy::new);
		MRV.setDefaultReturnValuesSupplier("format", IMOpsSpy::new);
		MRV.setDefaultReturnValuesSupplier("resize", IMOpsSpy::new);
		MRV.setDefaultReturnValuesSupplier("quality", IMOpsSpy::new);
		MRV.setDefaultReturnValuesSupplier("thumbnail", IMOpsSpy::new);
		MRV.setDefaultReturnValuesSupplier("alpha", IMOpsSpy::new);
		MRV.setDefaultReturnValuesSupplier("define", IMOpsSpy::new);
	}

	@Override
	public Operation addImage(String... arg0) {
		callsInOrder.addAll(Arrays.asList(arg0));
		return (Operation) MCR.addCallAndReturnFromMRV("arg0", arg0);
	}

	@Override
	public IMOps format(String arg0) {
		callsInOrder.add(arg0);
		return (IMOps) MCR.addCallAndReturnFromMRV("arg0", arg0);
	}

	@Override
	public IMOps resize(Integer var1, Integer var2) {
		return (IMOps) MCR.addCallAndReturnFromMRV("var1", var1, "var2", var2);
	}

	@Override
	public IMOps quality(Double var1) {
		return (IMOps) MCR.addCallAndReturnFromMRV("var1", var1);
	}

	@Override
	public IMOps thumbnail(Integer arg0) {
		return (IMOps) MCR.addCallAndReturnFromMRV("arg0", arg0);
	}

	@Override
	public IMOps alpha(String arg0) {
		return (IMOps) MCR.addCallAndReturnFromMRV("arg0", arg0);
	}

	@Override
	public IMOps define(String arg0) {
		return (IMOps) MCR.addCallAndReturnFromMRV("arg0", arg0);
	}
}
