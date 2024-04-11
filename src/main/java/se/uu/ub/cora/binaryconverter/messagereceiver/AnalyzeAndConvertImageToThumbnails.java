/*
 * Copyright 2023, 2024 Uppsala University Library
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

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreator;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.storage.StreamPathBuilder;
import se.uu.ub.cora.storage.archive.ArchivePathBuilder;

public class AnalyzeAndConvertImageToThumbnails implements MessageReceiver {
	private static final int HTTP_CONFLICT = 409;
	private static final String THUMBNAIL = "thumbnail";
	private static final String MEDIUM = "medium";
	private static final String LARGE = "large";
	private static final int THUMBNAIL_SIZE = 100;
	private static final int MEDIUM_SIZE = 300;
	private static final int LARGE_SIZE = 600;
	private Logger logger = LoggerProvider
			.getLoggerForClass(AnalyzeAndConvertImageToThumbnails.class);
	private DataClient dataClient;
	private BinaryOperationFactory binaryOperationFactory;
	private ResourceMetadataCreator resourceMetadataCreator;
	private ArchivePathBuilder archivePathBuilder;
	private StreamPathBuilder streamPathBuilder;

	public AnalyzeAndConvertImageToThumbnails(DataClient dataClient,
			BinaryOperationFactory binaryOperationFactory, ArchivePathBuilder archivePathBuilder,
			StreamPathBuilder streamPathBuilder, ResourceMetadataCreator resourceMetadataCreator) {
		this.dataClient = dataClient;
		this.binaryOperationFactory = binaryOperationFactory;
		this.archivePathBuilder = archivePathBuilder;
		this.streamPathBuilder = streamPathBuilder;
		this.resourceMetadataCreator = resourceMetadataCreator;
	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {
		String recordType = headers.get("type");
		String recordId = headers.get("id");
		String dataDivider = headers.get("dataDivider");
		String originalImagePath = archivePathBuilder.buildPathToAResourceInArchive(dataDivider,
				recordType, recordId);

		ImageData masterImageData = analyzeImage(originalImagePath);

		var representations = convertAndCreateMetadataForRepresentations(dataDivider, recordType,
				recordId, originalImagePath);

		addRepresentationDataToRecordAndUpdate(recordType, recordId, masterImageData,
				representations);
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = binaryOperationFactory.factorImageAnalyzer(pathToImage);
		return analyzer.analyze();
	}

	private Map<String, ClientDataGroup> convertAndCreateMetadataForRepresentations(
			String dataDivider, String type, String recordId, String inputPath) {
		ClientDataGroup largeRepresentation = convertRepresentation(dataDivider, type, recordId,
				LARGE, inputPath, LARGE_SIZE);
		/**
		 * To increase speed and efficiency of the conversion process we use the large preview
		 * version to convert the medium and thumbnail versions instead of the archived version.
		 */
		String largePath = getPathToLargeRepresentation(dataDivider, type, recordId);
		ClientDataGroup mediumRepresentation = convertRepresentation(dataDivider, type, recordId,
				MEDIUM, largePath, MEDIUM_SIZE);
		ClientDataGroup thumbnailRepresentation = convertRepresentation(dataDivider, type, recordId,
				THUMBNAIL, largePath, THUMBNAIL_SIZE);

		return representationGroupstoMap(largeRepresentation, mediumRepresentation,
				thumbnailRepresentation);
	}

	private ClientDataGroup convertRepresentation(String dataDivider, String type, String recordId,
			String representation, String inputPath, int size) {
		String outputPath = streamPathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider,
				type, recordId + "-" + representation);
		return convertImageUsingResourceTypeNameAndWidth(recordId, inputPath, outputPath,
				representation, size);
	}

	private ClientDataGroup convertImageUsingResourceTypeNameAndWidth(String recordId,
			String pathToImage, String outputPath, String representation, int convertToWidth) {
		ImageConverter imageConverter = binaryOperationFactory.factorImageConverter();
		imageConverter.convertAndResizeUsingWidth(pathToImage, outputPath, convertToWidth);

		ImageData imageData = analyzeImage(outputPath);

		return resourceMetadataCreator.createMetadataForRepresentation(representation, recordId,
				imageData, "image/jpeg");
	}

	private String getPathToLargeRepresentation(String dataDivider, String type, String recordId) {
		return streamPathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-" + LARGE);
	}

	private Map<String, ClientDataGroup> representationGroupstoMap(
			ClientDataGroup largeRepresentation, ClientDataGroup mediumRepresentation,
			ClientDataGroup thumbnailRepresentation) {
		Map<String, ClientDataGroup> representations = new HashMap<>();
		representations.put(LARGE, largeRepresentation);
		representations.put(MEDIUM, mediumRepresentation);
		representations.put(THUMBNAIL, thumbnailRepresentation);
		return representations;
	}

	private void addRepresentationDataToRecordAndUpdate(String recordType, String recordId,
			ImageData masterImageData, Map<String, ClientDataGroup> representations) {
		ClientDataRecordGroup binaryRecordGroup = addRepresentationsDataToRecord(recordType,
				recordId, masterImageData, representations);
		try {
			dataClient.update(recordType, recordId, binaryRecordGroup);
		} catch (DataClientException dataClientException) {
			throwExceptionIfNotConflict(recordId, dataClientException);
			retryRecordUpdate(recordType, recordId, masterImageData, representations);
		}
	}

	private ClientDataRecordGroup addRepresentationsDataToRecord(String recordType, String recordId,
			ImageData masterImageData, Map<String, ClientDataGroup> representations) {
		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);
		addMasterRepresentationDataToRecord(masterImageData, binaryRecordGroup);
		addOtherRepresentationDataToRecord(representations, binaryRecordGroup);
		return binaryRecordGroup;
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private void addMasterRepresentationDataToRecord(ImageData masterImageData,
			ClientDataRecordGroup binaryRecordGroup) {
		ClientDataGroup masterG = binaryRecordGroup.getFirstGroupWithNameInData("master");
		resourceMetadataCreator.updateMasterGroup(masterG, masterImageData);
	}

	private void addOtherRepresentationDataToRecord(Map<String, ClientDataGroup> representations,
			ClientDataRecordGroup binaryRecordGroup) {
		binaryRecordGroup.addChild(representations.get(LARGE));
		binaryRecordGroup.addChild(representations.get(MEDIUM));
		binaryRecordGroup.addChild(representations.get(THUMBNAIL));
	}

	private void throwExceptionIfNotConflict(String recordId,
			DataClientException dataClientException) {
		if (isDifferentThanRecordConflict(dataClientException)) {
			throw createBinaryConverterExecption(recordId, dataClientException);
		}
	}

	private boolean isDifferentThanRecordConflict(DataClientException dataClientException) {
		var optionalResponseCode = dataClientException.getResponseCode();
		if (optionalResponseCode.isEmpty()) {
			return true;
		}
		return optionalResponseCode.get() != HTTP_CONFLICT;
	}

	private BinaryConverterException createBinaryConverterExecption(String recordId,
			DataClientException dataClientException) {
		return BinaryConverterException.withMessageAndException("Binary record with id: " + recordId
				+ " could not be updated with conversion data.", dataClientException);
	}

	private void retryRecordUpdate(String recordType, String recordId, ImageData masterImageData,
			Map<String, ClientDataGroup> representations) {
		logger.logInfoUsingMessage("Binary record with id: " + recordId
				+ " could not be updated due to record conflict. Retrying record update.");
		addRepresentationDataToRecordAndUpdate(recordType, recordId, masterImageData,
				representations);
	}

	@Override
	public void topicClosed() {
		logger.logFatalUsingMessage("Topic is closed!");
	}

	DataClient onlyForTestGetDataClient() {
		return dataClient;
	}

	BinaryOperationFactory onlyForTestGetBinaryOperationFactory() {
		return binaryOperationFactory;
	}

	ArchivePathBuilder onlyForTestGetArchivePathBuilder() {
		return archivePathBuilder;
	}

	StreamPathBuilder onlyForTestGetStreamPathBuilder() {
		return streamPathBuilder;
	}

	ResourceMetadataCreator onlyForTestGetResourceMetadataCreator() {
		return resourceMetadataCreator;
	}

}
