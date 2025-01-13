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

import java.util.Collections;
import java.util.List;

import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpjParameters;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class OpjParametersSpy implements OpjParameters {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public OpjParametersSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("getParamsList", Collections::emptyList);
		MRV.setDefaultReturnValuesSupplier("getInputPath", () -> "someInputPath");
		MRV.setDefaultReturnValuesSupplier("getOutputPath", () -> "someOutputPath");
	}

	@Override
	public List<String> getParamsList() {
		return (List<String>) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public String getInputPath() {
		return (String) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public void inputPath(String inputPath) {
		MCR.addCall("inputPath", inputPath);

	}

	@Override
	public String getOutputPath() {
		return (String) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public void outputPath(String outputPath) {
		MCR.addCall("outputPath", outputPath);

	}

	@Override
	public void psnrQuality(int... psnrLayers) {
		MCR.addCall("psnrLayers", psnrLayers);

	}

	@Override
	public void compressionRatio(int... ratio) {
		MCR.addCall("ratio", ratio);

	}

	@Override
	public void tileSize(int width, int height) {
		MCR.addCall("width", width, "height", height);
	}

	@Override
	public void numOfResolutions(int numOfResolutions) {
		MCR.addCall("numOfResolutions", numOfResolutions);
	}

	@Override
	public void precinctSize(int... precinctSize) {
		MCR.addCall("precinctSize", precinctSize);
	}

	@Override
	public void codeBlockSize(int width, int height) {
		MCR.addCall("width", width, "height", height);
	}

	@Override
	public void progressionOrder(String progressionOrderName) {
		MCR.addCall("progressionOrderName", progressionOrderName);
	}

	@Override
	public void enableSop() {
		MCR.addCall();
	}

	@Override
	public void enableEph() {
		MCR.addCall();
	}

	@Override
	public void enablePlt() {
		MCR.addCall();
	}

	@Override
	public void enableTlm() {
		MCR.addCall();
	}

	@Override
	public void tilePartDivider(String type) {
		MCR.addCall("type", type);
	}

	@Override
	public void numberOfThreads(int numOfThreads) {
		MCR.addCall("numOfThreads", numOfThreads);
	}

	@Override
	public void opjCommand(String command) {
		MCR.addCall("command", command);
	}

}
