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
package se.uu.ub.cora.binaryconverter.messagereceiver;

import java.util.Map;

import se.uu.ub.cora.binaryconverter.common.PathBuilder;
import se.uu.ub.cora.binaryconverter.common.ResourceMetadataCreator;
import se.uu.ub.cora.binaryconverter.document.Jp2ConverterFactory;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class ConvertImageToJp2 implements MessageReceiver {

	private static final String JP2_MIME_TYPE = "image/jp2";
	private Jp2ConverterFactory jp2ConverterFactory;
	private ImageAnalyzerFactory imageAnalyzerFactory;
	private DataClient dataClient;
	private ResourceMetadataCreator resourceMetadataCreator;
	private PathBuilder pathBuilder;

	public ConvertImageToJp2(Jp2ConverterFactory jp2ConverterFactory,
			ImageAnalyzerFactory imageAnalyzerFactory, DataClient dataClient,
			ResourceMetadataCreator resourceMetadataCreator, PathBuilder pathBuilder) {
		this.jp2ConverterFactory = jp2ConverterFactory;
		this.imageAnalyzerFactory = imageAnalyzerFactory;
		this.dataClient = dataClient;
		this.resourceMetadataCreator = resourceMetadataCreator;
		this.pathBuilder = pathBuilder;

	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {
		String recordType = headers.get("type");
		String recordId = headers.get("id");
		String dataDivider = headers.get("dataDivider");
		String originalImagePath = pathBuilder.buildPathToAResourceInArchive(dataDivider,
				recordType, recordId);

		ImageData imageData = convertAndAnalyzeImage(dataDivider, recordType, recordId,
				originalImagePath);

		ClientDataRecordGroup binaryRecordGroup = createMetadataForRepresentation(recordType,
				recordId, imageData);
		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private ImageData convertAndAnalyzeImage(String dataDivider, String type, String recordId,
			String inputPath) {
		String largePath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-jp2");

		return convertToJp2AndAnalyze(inputPath, largePath);
	}

	private ImageData convertToJp2AndAnalyze(String pathToImage, String outputPath) {
		Jp2Converter jp2Converter = jp2ConverterFactory.factor();
		jp2Converter.convert(pathToImage, outputPath);

		return analyzeImage(outputPath);
	}

	private ClientDataRecordGroup createMetadataForRepresentation(String recordType,
			String recordId, ImageData imageData) {
		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);
		ClientDataGroup resourceInfoGroup = binaryRecordGroup
				.getFirstGroupWithNameInData("resourceInfo");
		resourceMetadataCreator.createMetadataForRepresentation("jp2", resourceInfoGroup, recordId,
				imageData, JP2_MIME_TYPE);
		return binaryRecordGroup;
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = imageAnalyzerFactory.factor(pathToImage);
		return analyzer.analyze();
	}

	@Override
	public void topicClosed() {
		// TODO Auto-generated method stub

	}

	public Jp2ConverterFactory onlyForTestGetJp2ConverterFactory() {
		return jp2ConverterFactory;
	}

	public ImageAnalyzerFactory onlyForTestGetImageAnalyzerFactory() {
		return imageAnalyzerFactory;
	}

	public Object onlyForTestGetDataClient() {
		return dataClient;
	}

	public ResourceMetadataCreator onlyForTestGetResourceMetadataCreator() {
		return resourceMetadataCreator;
	}

	public PathBuilder onlyForTestGetPathBuilder() {
		return pathBuilder;
	}

}
