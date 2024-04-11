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

import java.util.Map;

import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
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

public class ConvertImageToJp2 implements MessageReceiver {
	private static final int HTTP_CONFLICT = 409;
	private Logger logger = LoggerProvider.getLoggerForClass(ConvertImageToJp2.class);
	private BinaryOperationFactory binaryOperationFactory;
	private DataClient dataClient;
	private ResourceMetadataCreator resourceMetadataCreator;
	private ArchivePathBuilder archivePathBuilder;
	private StreamPathBuilder streamPathBuilder;

	public ConvertImageToJp2(BinaryOperationFactory binaryOperationFactory, DataClient dataClient,
			ResourceMetadataCreator resourceMetadataCreator, ArchivePathBuilder archivePathBuilder,
			StreamPathBuilder streamPathBuilder) {
		this.binaryOperationFactory = binaryOperationFactory;
		this.dataClient = dataClient;
		this.resourceMetadataCreator = resourceMetadataCreator;
		this.archivePathBuilder = archivePathBuilder;
		this.streamPathBuilder = streamPathBuilder;
	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {
		String recordType = headers.get("type");
		String recordId = headers.get("id");
		String dataDivider = headers.get("dataDivider");
		String mimeType = headers.get("mimeType");
		String originalImagePath = archivePathBuilder.buildPathToAResourceInArchive(dataDivider,
				recordType, recordId);

		ImageData imageData = convertAndAnalyzeImage(dataDivider, recordType, recordId,
				originalImagePath, mimeType);
		ClientDataGroup jp2Group = resourceMetadataCreator.createMetadataForRepresentation("jp2",
				recordId, imageData, "image/jp2");

		updateRecordUsingRepresentationDataGroup(recordType, recordId, jp2Group);
	}

	private ImageData convertAndAnalyzeImage(String dataDivider, String type, String recordId,
			String inputPath, String mimeType) {
		String largePath = streamPathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider,
				type, recordId + "-jp2");

		return convertToJp2AndAnalyze(inputPath, largePath, mimeType);
	}

	private ImageData convertToJp2AndAnalyze(String pathToImage, String outputPath,
			String mimeType) {
		Jp2Converter jp2Converter = binaryOperationFactory.factorJp2Converter();
		jp2Converter.convert(pathToImage, outputPath, mimeType);

		return analyzeImage(outputPath);
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = binaryOperationFactory.factorImageAnalyzer(pathToImage);
		return analyzer.analyze();
	}

	private void updateRecordUsingRepresentationDataGroup(String recordType, String recordId,
			ClientDataGroup jp2Group) {
		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);
		binaryRecordGroup.addChild(jp2Group);
		tryToUpdateRecord(recordType, recordId, jp2Group, binaryRecordGroup);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private void tryToUpdateRecord(String recordType, String recordId, ClientDataGroup jp2Group,
			ClientDataRecordGroup binaryRecordGroup) {
		try {
			dataClient.update(recordType, recordId, binaryRecordGroup);
		} catch (DataClientException dataClientException) {
			throwExceptionIfNotConflict(recordId, dataClientException);
			retryRecordUpdate(recordType, recordId, jp2Group);
		}
	}

	private void throwExceptionIfNotConflict(String recordId,
			DataClientException dataClientException) {
		if (isDifferentThanRecordConflict(dataClientException)) {
			throw createBinaryConverterException(recordId, dataClientException);
		}
	}

	private void retryRecordUpdate(String recordType, String recordId, ClientDataGroup jp2Group) {
		logger.logInfoUsingMessage("Binary record with id: " + recordId
				+ " could not be updated due to record conflict. Retrying record update.");
		updateRecordUsingRepresentationDataGroup(recordType, recordId, jp2Group);
	}

	private boolean isDifferentThanRecordConflict(DataClientException dataClientException) {
		var optionalResponseCode = dataClientException.getResponseCode();
		if (optionalResponseCode.isEmpty()) {
			return true;
		}
		return optionalResponseCode.get() != HTTP_CONFLICT;
	}

	private BinaryConverterException createBinaryConverterException(String recordId,
			DataClientException dataClientException) {
		return BinaryConverterException.withMessageAndException("Binary record with id: " + recordId
				+ " could not be updated with jp2 conversion data.", dataClientException);
	}

	@Override
	public void topicClosed() {
		logger.logFatalUsingMessage("Topic is closed!");
	}

	public BinaryOperationFactory onlyForTestGetBinaryOperationFactory() {
		return binaryOperationFactory;
	}

	public Object onlyForTestGetDataClient() {
		return dataClient;
	}

	public ResourceMetadataCreator onlyForTestGetResourceMetadataCreator() {
		return resourceMetadataCreator;
	}

	ArchivePathBuilder onlyForTestGetArchivePathBuilder() {
		return archivePathBuilder;
	}

	StreamPathBuilder onlyForTestGetStreamPathBuilder() {
		return streamPathBuilder;
	}

}
