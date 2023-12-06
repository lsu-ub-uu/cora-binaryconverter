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
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.ImageConverterFactory;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertImageToThumbnails implements MessageReceiver {
	private static final String JPEG_MIME_TYPE = "image/jpeg";
	private ImageAnalyzerFactory imageAnalyzerFactory;
	private DataClient dataClient;
	private ImageConverterFactory imageConverterFactory;
	private PathBuilder pathBuilder;
	private ResourceMetadataCreator resourceMetadataCreator;

	public AnalyzeAndConvertImageToThumbnails(DataClient dataClient,
			ImageAnalyzerFactory imageAnalyzerFactory, ImageConverterFactory imageConverterFactory,
			PathBuilder pathBuilder, ResourceMetadataCreator resourceMetadataCreator) {
		this.dataClient = dataClient;
		this.imageAnalyzerFactory = imageAnalyzerFactory;
		this.imageConverterFactory = imageConverterFactory;
		this.pathBuilder = pathBuilder;
		this.resourceMetadataCreator = resourceMetadataCreator;
	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {
		String recordType = headers.get("type");
		String recordId = headers.get("id");
		String dataDivider = headers.get("dataDivider");
		String originalImagePath = pathBuilder.buildPathToAResourceInArchive(dataDivider,
				recordType, recordId);

		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);
		ClientDataGroup resourceInfoGroup = binaryRecordGroup
				.getFirstGroupWithNameInData("resourceInfo");

		analyzeAndUpdateMetadataForMasterRepresentation(originalImagePath, resourceInfoGroup);

		convertAndCreateMetadataForRepresentations(dataDivider, recordType, recordId,
				resourceInfoGroup, originalImagePath);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private void analyzeAndUpdateMetadataForMasterRepresentation(String originalImagePath,
			ClientDataGroup resourceInfoGroup) {
		ImageData masterImageData = analyzeImage(originalImagePath);
		resourceMetadataCreator.updateMasterGroupFromResourceInfo(resourceInfoGroup,
				masterImageData);
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = imageAnalyzerFactory.factor(pathToImage);
		return analyzer.analyze();
	}

	private void convertAndCreateMetadataForRepresentations(String dataDivider, String type,
			String recordId, ClientDataGroup resourceInfoGroup, String inputPath) {
		String largePath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-large");
		String mediumPath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-medium");
		String thumbnailPath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-thumbnail");

		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, inputPath, largePath,
				"large", 600);
		/**
		 * To increase speed and efficiency of the conversion process we use the large preview
		 * version to convert the medium and thumbnail versions instead of the archived version.
		 */
		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, largePath,
				mediumPath, "medium", 300);
		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, largePath,
				thumbnailPath, "thumbnail", 100);
	}

	private void convertImageUsingResourceTypeNameAndWidth(ClientDataGroup resourceInfoGroup,
			String recordId, String pathToImage, String outputPath, String representation,
			int convertToWidth) {

		ImageConverter imageConverter = imageConverterFactory.factor();
		imageConverter.convertUsingWidth(pathToImage, outputPath, convertToWidth);

		ImageData imageData = analyzeImage(outputPath);

		resourceMetadataCreator.createMetadataForRepresentation(representation, resourceInfoGroup,
				recordId, imageData, JPEG_MIME_TYPE);
	}

	@Override
	public void topicClosed() {
		// TODO Auto-generated method stub
	}

	ImageAnalyzerFactory onlyForTestGetImageAnalyzerFactory() {
		return imageAnalyzerFactory;
	}

	DataClient onlyForTestGetDataClient() {
		return dataClient;
	}

	ImageConverterFactory onlyForTestGetImageConverterFactory() {
		return imageConverterFactory;
	}

	PathBuilder onlyForTestGetPathBuilder() {
		return pathBuilder;
	}

	ResourceMetadataCreator onlyForTestGetResourceMetadataCreator() {
		return resourceMetadataCreator;
	}

}
