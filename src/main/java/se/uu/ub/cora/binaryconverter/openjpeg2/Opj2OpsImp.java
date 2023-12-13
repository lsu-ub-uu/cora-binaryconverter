package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Opj2OpsImp implements Opj2Ops {

	private List<String> ops;
	private String inputPath;
	private String outputPath;

	public Opj2OpsImp() {
		ops = new ArrayList<>();
	}

	@Override
	public List<String> getOpsList() {
		return ops;
	}

	@Override
	public String getInputPath() {
		return inputPath;
	}

	@Override
	public void inputPath(String inputPath) {
		this.inputPath = inputPath;
		ops.add("-i");
		ops.add(inputPath);
	}

	@Override
	public String getOutputPath() {
		return outputPath;
	}

	@Override
	public void outputPath(String outputPath) {
		this.outputPath = outputPath;
		ops.add("-o");
		ops.add(outputPath);
	}

	@Override
	public void psnrQuality(int... psnrLayers) {
		ops.add("-q");
		ops.add(valuesAsString(psnrLayers));
	}

	private String valuesAsString(int[] values) {
		return IntStream.of(values).mapToObj(Integer::toString).collect(Collectors.joining(","));
	}

	@Override
	public void compressionRatio(int... ratio) {
		ops.add("-r");
		ops.add(valuesAsString(ratio));
	}

	@Override
	public void tileSize(int width, int height) {
		ops.add("-t");
		ops.add(width + "," + height);
	}

	@Override
	public void numOfResolutions(int numOfResolutions) {
		ops.add("-n");
		ops.add(numOfResolutions + "");
	}

	@Override
	public void precinctSize(int width, int height) {
		ops.add("-c");
		ops.add("[" + width + "," + height + "]");
	}

	@Override
	public void codeBlockSize(int width, int height) {
		ops.add("-b");
		ops.add(width + "," + height);
	}

	@Override
	public void progressionOrder(String progressionOrderName) {
		ops.add("-p");
		ops.add(progressionOrderName.toUpperCase());
	}

	@Override
	public void enableSop() {
		ops.add("-SOP");
	}

	@Override
	public void enableEph() {
		ops.add("-EPH");
	}

	@Override
	public void numberOfThreads(int numOfThreads) {
		ops.add("-threads");
		ops.add(numOfThreads + "");
	}

	@Override
	public void enablePlt() {
		ops.add("-PLT");
	}

	@Override
	public void enableTlm() {
		ops.add("-TLM");
	}

	@Override
	public void tilePartDivider(String type) {
		ops.add("-TP");
		ops.add(type);
	}
}
