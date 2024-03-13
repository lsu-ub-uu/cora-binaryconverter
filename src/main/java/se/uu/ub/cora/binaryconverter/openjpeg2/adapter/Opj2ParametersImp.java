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
package se.uu.ub.cora.binaryconverter.openjpeg2.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Opj2ParametersImp implements Opj2Parameters {

	private static final int PAIR = 2;
	private List<String> params;
	private String inputPath;
	private String outputPath;

	public Opj2ParametersImp() {
		params = new ArrayList<>();
	}

	@Override
	public List<String> getParamsList() {
		return params;
	}

	@Override
	public String getInputPath() {
		return inputPath;
	}

	@Override
	public void inputPath(String inputPath) {
		this.inputPath = inputPath;
		params.add("-i");
		params.add(inputPath);
	}

	@Override
	public String getOutputPath() {
		return outputPath;
	}

	@Override
	public void outputPath(String outputPath) {
		this.outputPath = outputPath;
		params.add("-o");
		params.add(outputPath);
	}

	@Override
	public void psnrQuality(int... psnrLayers) {
		params.add("-q");
		params.add(valuesAsCommaSeparatedString(psnrLayers));
	}

	private String valuesAsCommaSeparatedString(int[] values) {
		return IntStream.of(values).mapToObj(Integer::toString).collect(Collectors.joining(","));
	}

	@Override
	public void compressionRatio(int... ratio) {
		params.add("-r");
		params.add(valuesAsCommaSeparatedString(ratio));
	}

	@Override
	public void tileSize(int width, int height) {
		params.add("-t");
		params.add(width + "," + height);
	}

	@Override
	public void numOfResolutions(int numOfResolutions) {
		params.add("-n");
		params.add(numOfResolutions + "");
	}

	@Override
	public void precinctSize(int... precinctSize) {
		params.add("-c");
		params.add(parsePrecinctSizes(precinctSize));
	}

	private String parsePrecinctSizes(int[] sizes) {
		StringBuilder stringOfPrecinctValues = new StringBuilder();
		for (int i = 0; i < sizes.length; i += PAIR) {
			stringPrefix(sizes, stringOfPrecinctValues, i);
			createPairs(sizes, stringOfPrecinctValues, i);
			possiblyAddCommaBetweenPairs(sizes, stringOfPrecinctValues, i);
		}
		return stringOfPrecinctValues.toString();
	}

	private void stringPrefix(int[] sizes, StringBuilder stringOfPrecinctValues, int i) {
		stringOfPrecinctValues.append("[" + sizes[i] + ",");
	}

	private void createPairs(int[] sizes, StringBuilder stringOfPrecinctValues, int i) {
		if (isNotARealPair(sizes, i)) {
			stringOfPrecinctValues.append(sizes[i] + "]");
		} else {
			stringOfPrecinctValues.append(sizes[i + 1] + "]");
		}
	}

	private boolean isNotARealPair(int[] sizes, int i) {
		return i == sizes.length - 1 && sizes.length % PAIR != 0;
	}

	private void possiblyAddCommaBetweenPairs(int[] sizes, StringBuilder stringOfPrecinctValues,
			int i) {
		if (existsNewPair(sizes, i)) {
			stringOfPrecinctValues.append(',');
		}
	}

	private boolean existsNewPair(int[] sizes, int i) {
		return i + PAIR < sizes.length;
	}

	@Override
	public void codeBlockSize(int width, int height) {
		params.add("-b");
		params.add(width + "," + height);
	}

	@Override
	public void progressionOrder(String progressionOrderName) {
		params.add("-p");
		params.add(progressionOrderName.toUpperCase());
	}

	@Override
	public void enableSop() {
		params.add("-SOP");
	}

	@Override
	public void enableEph() {
		params.add("-EPH");
	}

	@Override
	public void numberOfThreads(int numOfThreads) {
		params.add("-threads");
		if (numOfThreads > 0) {
			params.add(numOfThreads + "");
		} else {
			params.add("ALL_CPUS");
		}
	}

	@Override
	public void enablePlt() {
		params.add("-PLT");
	}

	@Override
	public void enableTlm() {
		params.add("-TLM");
	}

	@Override
	public void tilePartDivider(String type) {
		params.add("-TP");
		params.add(type.toUpperCase());
	}

	@Override
	public void opj2Command(String command) {
		params.add(0, command);
	}
}
