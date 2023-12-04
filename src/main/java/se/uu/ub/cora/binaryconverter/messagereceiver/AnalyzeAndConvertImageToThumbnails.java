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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private static final String CAN_NOT_WRITE_FILES_TO_DISK = "can not write files to disk: ";

	private ImageAnalyzerFactory imageAnalyzerFactory;
	private DataClient dataClient;
	private ImageConverterFactory imageConverterFactory;
	private String fileStorageBasePath;
	private PathBuilder pathBuilder;
	private ResourceMetadataCreator resourceMetadataCreator;

	public AnalyzeAndConvertImageToThumbnails(DataClient dataClient, String fileStorageBasePath,
			ImageAnalyzerFactory imageAnalyzerFactory, ImageConverterFactory imageConverterFactory,
			PathBuilder pathBuilder, ResourceMetadataCreator resourceMetadataCreator) {
		this.dataClient = dataClient;
		this.fileStorageBasePath = fileStorageBasePath;
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
		String originalImagePath = pathBuilder.buildPathToAResourceInArchive(recordType, recordId,
				dataDivider);

		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);
		ClientDataGroup resourceInfoGroup = binaryRecordGroup
				.getFirstGroupWithNameInData("resourceInfo");

		analyzeAndUpdateMetadataForMasterRepresentation(originalImagePath, resourceInfoGroup);

		Path pathByDataDivider = Paths.get(fileStorageBasePath, "streams", dataDivider);
		ensureStorageDirectoryExists(Paths.get(fileStorageBasePath, "streams"));
		ensureStorageDirectoryExists(pathByDataDivider);

		convertAndCreateMetadataForRepresentations(recordId, dataDivider, resourceInfoGroup,
				originalImagePath);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private void ensureStorageDirectoryExists(Path pathByDataDivider) {
		if (storageDirectoryDoesNotExist(pathByDataDivider)) {
			tryToCreateStorageDirectory(pathByDataDivider);
		}
	}

	private boolean storageDirectoryDoesNotExist(Path pathByDataDivider) {
		return !Files.exists(pathByDataDivider);
	}

	private void tryToCreateStorageDirectory(Path pathByDataDivider) {
		try {
			Files.createDirectory(pathByDataDivider);
		} catch (IOException e) {
			throw new RuntimeException(CAN_NOT_WRITE_FILES_TO_DISK + e, e);
		}
	}

	private void analyzeAndUpdateMetadataForMasterRepresentation(String originalImagePath,
			ClientDataGroup resourceInfoGroup) {
		ImageData masterImageData = analyzeImage(originalImagePath);
		resourceMetadataCreator.updateMasterGroupFromResourceInfo(resourceInfoGroup,
				masterImageData);
	}

	private void convertAndCreateMetadataForRepresentations(String recordId, String dataDivider,
			ClientDataGroup resourceInfoGroup, String inputPath) {
		String fileStoragePathToAResourceId = buildFileStoragePathToAResourceId(recordId,
				dataDivider);
		String largeRepresentationPath = fileStoragePathToAResourceId + "-large";

		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, inputPath,
				fileStoragePathToAResourceId, "large", 600);
		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId,
				largeRepresentationPath, fileStoragePathToAResourceId, "medium", 300);
		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId,
				largeRepresentationPath, fileStoragePathToAResourceId, "thumbnail", 100);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private String buildFileStoragePathToAResourceId(String recordId, String dataDivider) {
		return fileStorageBasePath + "streams/" + dataDivider + "/" + recordId;
	}

	private void convertImageUsingResourceTypeNameAndWidth(ClientDataGroup resourceInfoGroup,
			String recordId, String pathToImage, String outputPath, String representation,
			int convertToWidth) {

		ImageConverter imageConverter = imageConverterFactory.factor();
		imageConverter.convertUsingWidth(pathToImage, outputPath + "-" + representation,
				convertToWidth);

		ImageData imageData = analyzeImage(outputPath + "-" + representation);

		resourceMetadataCreator.createMetadataForRepresentation(representation, resourceInfoGroup,
				recordId, imageData);
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = imageAnalyzerFactory.factor(pathToImage);
		return analyzer.analyze();
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

	String onlyForTestGetFileStorageBasePath() {
		return fileStorageBasePath;
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
