package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Opj2ParametersImp implements Opj2Parameters {

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
		for (int i = 0; i < sizes.length; i += 2) {
			stringOfPrecinctValues.append("[" + sizes[i] + ",");

			if (i == sizes.length - 1 && sizes.length % 2 != 0) {
				stringOfPrecinctValues.append(sizes[i] + "]");
			} else {
				stringOfPrecinctValues.append(sizes[i + 1] + "]");
			}

			if (i + 2 < sizes.length) {
				stringOfPrecinctValues.append(",");
			}
		}
		return stringOfPrecinctValues.toString();
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
}
