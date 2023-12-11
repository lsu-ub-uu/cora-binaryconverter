package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.LinkedList;

public class Opj2OpsImp implements Opj2Ops {

	private LinkedList<String> ops;
	private String inputPath;
	private String outputPath;

	public Opj2OpsImp() {
		ops = new LinkedList<>();
	}

	@Override
	public LinkedList<String> getOpsList() {
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
	public void psnrQuality(String psnrLayers) {
		ops.add("-q");
		ops.add(psnrLayers);
	}

	@Override
	public void tileSize(String tileSize) {
		ops.add("-t");
		ops.add(tileSize);
	}

	@Override
	public void numOfResolutions(int numOfResolutions) {
		ops.add("-n");
		ops.add(numOfResolutions + "");
	}

	@Override
	public void precinctSize(String precinctSize) {
		ops.add("-c");
		ops.add(precinctSize);
	}

	@Override
	public void codeBlockSize(String cblSize) {
		ops.add("-b");
		ops.add(cblSize);
	}

	@Override
	public void progressionOrder(String progressionOrderName) {
		ops.add("-p");
		ops.add(progressionOrderName.toUpperCase());
	}

	@Override
	public void enableSop(boolean enableSop) {
		ops.add("-SOP");
	}

	@Override
	public void enableEph(boolean enableEph) {
		ops.add("-EPH");
	}
}
