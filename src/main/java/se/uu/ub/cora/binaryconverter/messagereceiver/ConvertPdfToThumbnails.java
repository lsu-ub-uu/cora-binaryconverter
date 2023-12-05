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
import se.uu.ub.cora.binaryconverter.document.PdfConverter;
import se.uu.ub.cora.binaryconverter.document.PdfConverterFactory;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzer;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class ConvertPdfToThumbnails implements MessageReceiver {

	private DataClient dataClient;
	private PdfConverterFactory pdfConverterFactory;
	private ImageAnalyzerFactory imageAnalyzerFactory;
	private PathBuilder pathBuilder;
	private ResourceMetadataCreator resourceMetadataCreator;

	public ConvertPdfToThumbnails(PdfConverterFactory pdfConverterFactory,
			ImageAnalyzerFactory imageAnalyzerFactory, DataClient dataClient,
			ResourceMetadataCreator resourceMetadataCreator, PathBuilder pathBuilder) {
		this.imageAnalyzerFactory = imageAnalyzerFactory;
		this.dataClient = dataClient;
		this.pdfConverterFactory = pdfConverterFactory;
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

		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);
		ClientDataGroup resourceInfoGroup = binaryRecordGroup
				.getFirstGroupWithNameInData("resourceInfo");

		convertAndCreateMetadataForRepresentations(dataDivider, recordType, recordId,
				resourceInfoGroup, originalImagePath);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private void convertAndCreateMetadataForRepresentations(String dataDivider, String type,
			String recordId, ClientDataGroup resourceInfoGroup, String inputPath) {
		String largePath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-large");
		String mediumPath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-medium");
		String thumbnailPath = pathBuilder.buildPathToAFileAndEnsureFolderExists(dataDivider, type,
				recordId + "-thumbnail");

		convertPdfUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, inputPath, largePath,
				"large", 600);
		/**
		 * To increase speed and efficiency of the conversion process we use the large preview
		 * version to convert the medium and thumbnail versions instead of the archived version.
		 */
		convertPdfUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, largePath, mediumPath,
				"medium", 300);
		convertPdfUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, largePath,
				thumbnailPath, "thumbnail", 100);
	}

	private void convertPdfUsingResourceTypeNameAndWidth(ClientDataGroup resourceInfoGroup,
			String recordId, String pathToImage, String outputPath, String representation,
			int convertToWidth) {

		PdfConverter pdfConverter = pdfConverterFactory.factor();
		pdfConverter.convertUsingWidth(pathToImage, outputPath, convertToWidth);

		ImageData imageData = analyzeImage(outputPath);

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

	public ImageAnalyzerFactory onlyForTestGetImageAnalyzerFactory() {
		return imageAnalyzerFactory;
	}

	public PdfConverterFactory onlyForTestGetPdfConverterFactory() {
		return pdfConverterFactory;
	}

	public DataClient onlyForTestGetDataClient() {
		return dataClient;
	}

	public ResourceMetadataCreator onlyForTestGetResourceMetadataCreator() {
		return resourceMetadataCreator;
	}

	public PathBuilder onlyForTestGetPathBuilder() {
		return pathBuilder;
	}

}
