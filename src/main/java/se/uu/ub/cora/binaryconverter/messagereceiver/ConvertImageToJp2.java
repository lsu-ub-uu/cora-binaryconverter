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
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.image.Jp2Converter;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreator;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.storage.StreamPathBuilder;
import se.uu.ub.cora.storage.archive.ArchivePathBuilder;

public class ConvertImageToJp2 implements MessageReceiver {
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

		ClientDataGroup jp2G = resourceMetadataCreator.createMetadataForRepresentation("jp2",
				recordId, imageData, "image/jp2");

		updateRecordUsingRepresentationDataGroup(recordType, recordId, jp2G);

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

	private void updateRecordUsingRepresentationDataGroup(String recordType, String recordId,
			ClientDataGroup jp2G) {
		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);

		binaryRecordGroup.addChild(jp2G);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
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
