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

import se.uu.ub.cora.binaryconverter.document.PdfConverter;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreator;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.storage.StreamPathBuilder;
import se.uu.ub.cora.storage.archive.ArchivePathBuilder;

public class ConvertPdfToThumbnails implements MessageReceiver {
	private DataClient dataClient;
	private BinaryOperationFactory binaryOperationFactory;
	private ArchivePathBuilder archivePathBuilder;
	private ResourceMetadataCreator resourceMetadataCreator;
	private StreamPathBuilder streamPathBuilder;

	public ConvertPdfToThumbnails(BinaryOperationFactory binaryOperationFactory,
			DataClient dataClient, ResourceMetadataCreator resourceMetadataCreator,
			ArchivePathBuilder archivePathBuilder, StreamPathBuilder streamPathBuilder) {
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
		String originalImagePath = archivePathBuilder.buildPathToAResourceInArchive(dataDivider,
				recordType, recordId);

		ClientDataRecordGroup binaryRecordGroup = convertAndCreateMetadataForRepresentations(
				dataDivider, recordType, recordId, originalImagePath);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private ClientDataRecordGroup convertAndCreateMetadataForRepresentations(String dataDivider,
			String type, String recordId, String inputPath) {
		String largePath = streamPathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider,
				type, recordId + "-large");
		String mediumPath = streamPathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider,
				type, recordId + "-medium");
		String thumbnailPath = streamPathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider,
				type, recordId + "-thumbnail");

		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(type, recordId);

		ClientDataGroup largeDG = convertToImagesAnalyzeAndCreateMetadataRepresentationGroup(
				recordId, inputPath, largePath, "large", 600);
		/**
		 * To increase speed and efficiency of the conversion process we use the large preview
		 * version to convert the medium and thumbnail versions instead of the archived version.
		 */
		ClientDataGroup mediumDG = convertToImagesAnalyzeAndCreateMetadataRepresentationGroup(
				recordId, largePath, mediumPath, "medium", 300);
		ClientDataGroup thumbnailDG = convertToImagesAnalyzeAndCreateMetadataRepresentationGroup(
				recordId, largePath, thumbnailPath, "thumbnail", 100);

		binaryRecordGroup.addChild(largeDG);
		binaryRecordGroup.addChild(mediumDG);
		binaryRecordGroup.addChild(thumbnailDG);

		return binaryRecordGroup;
	}

	private ClientDataGroup convertToImagesAnalyzeAndCreateMetadataRepresentationGroup(
			String recordId, String pathToImage, String outputPath, String representation,
			int convertToWidth) {

		PdfConverter pdfConverter = binaryOperationFactory.factorPdfConverter();
		pdfConverter.convertUsingWidth(pathToImage, outputPath, convertToWidth);

		ImageData imageData = analyzeImage(outputPath);

		return resourceMetadataCreator.createMetadataForRepresentation(representation, recordId,
				imageData, "image/jpeg");
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = binaryOperationFactory.factorImageAnalyzer(pathToImage);
		return analyzer.analyze();
	}

	@Override
	public void topicClosed() {
		// TODO Auto-generated method stub

	}

	public BinaryOperationFactory onlyForTestGetBinaryOperationFactory() {
		return binaryOperationFactory;
	}

	public DataClient onlyForTestGetDataClient() {
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
