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

import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageConverter;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.PathBuilder;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreator;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertImageToThumbnails implements MessageReceiver {
	private DataClient dataClient;
	private BinaryOperationFactory binaryOperationFactory;
	private PathBuilder pathBuilder;
	private ResourceMetadataCreator resourceMetadataCreator;

	public AnalyzeAndConvertImageToThumbnails(DataClient dataClient,
			BinaryOperationFactory binaryOperationFactory, PathBuilder pathBuilder,
			ResourceMetadataCreator resourceMetadataCreator) {
		this.dataClient = dataClient;
		this.binaryOperationFactory = binaryOperationFactory;
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

		analyzeAndUpdateMetadataForMasterRepresentation(originalImagePath, binaryRecordGroup);

		convertAndCreateMetadataForRepresentations(dataDivider, recordType, recordId,
				originalImagePath, binaryRecordGroup);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private void analyzeAndUpdateMetadataForMasterRepresentation(String originalImagePath,
			ClientDataRecordGroup binaryRecordGroup) {
		ImageData masterImageData = analyzeImage(originalImagePath);

		ClientDataGroup masterG = binaryRecordGroup.getFirstGroupWithNameInData("master");
		resourceMetadataCreator.updateMasterGroup(masterG, masterImageData);
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = binaryOperationFactory.factorImageAnalyzer(pathToImage);
		return analyzer.analyze();
	}

	private void convertAndCreateMetadataForRepresentations(String dataDivider, String type,
			String recordId, String inputPath, ClientDataRecordGroup binaryRecordGroup) {
		String largePath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-large");
		String mediumPath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-medium");
		String thumbnailPath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-thumbnail");

		ClientDataGroup largeG = convertImageUsingResourceTypeNameAndWidth(recordId, inputPath,
				largePath, "large", 600);
		/**
		 * To increase speed and efficiency of the conversion process we use the large preview
		 * version to convert the medium and thumbnail versions instead of the archived version.
		 */
		ClientDataGroup mediumG = convertImageUsingResourceTypeNameAndWidth(recordId, largePath,
				mediumPath, "medium", 300);
		ClientDataGroup thumbnailG = convertImageUsingResourceTypeNameAndWidth(recordId, largePath,
				thumbnailPath, "thumbnail", 100);

		binaryRecordGroup.addChild(largeG);
		binaryRecordGroup.addChild(mediumG);
		binaryRecordGroup.addChild(thumbnailG);

	}

	private ClientDataGroup convertImageUsingResourceTypeNameAndWidth(String recordId,
			String pathToImage, String outputPath, String representation, int convertToWidth) {

		ImageConverter imageConverter = binaryOperationFactory.factorImageConverter();
		imageConverter.convertUsingWidth(pathToImage, outputPath, convertToWidth);

		ImageData imageData = analyzeImage(outputPath);

		return resourceMetadataCreator.createMetadataForRepresentation(representation, recordId,
				imageData, "image/jpeg");
	}

	@Override
	public void topicClosed() {
		// TODO Auto-generated method stub
	}

	DataClient onlyForTestGetDataClient() {
		return dataClient;
	}

	BinaryOperationFactory onlyForTestGetBinaryOperationFactory() {
		return binaryOperationFactory;
	}

	PathBuilder onlyForTestGetPathBuilder() {
		return pathBuilder;
	}

	ResourceMetadataCreator onlyForTestGetResourceMetadataCreator() {
		return resourceMetadataCreator;
	}

}
