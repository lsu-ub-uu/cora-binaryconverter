/*
 * Copyright 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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

import se.uu.ub.cora.binaryconverter.common.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.common.PathBuilder;
import se.uu.ub.cora.binaryconverter.common.PathBuilderImp;
import se.uu.ub.cora.binaryconverter.common.ResourceMetadataCreator;
import se.uu.ub.cora.binaryconverter.common.ResourceMetadataCreatorImp;
import se.uu.ub.cora.binaryconverter.document.PdfConverterFactory;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.image.ImageConverterFactory;
import se.uu.ub.cora.binaryconverter.imagemagick.document.PdfConverterFactoryImp;
import se.uu.ub.cora.binaryconverter.imagemagick.image.ImageAnalyzerFactoryImp;
import se.uu.ub.cora.binaryconverter.imagemagick.image.ImageConverterFactoryImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientProvider;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class MessageReceiverFactoryImp implements MessageReceiverFactory {

	private ImageAnalyzerFactory imageAnalyzerFactory;
	private ResourceMetadataCreator resourceMetadataCreator;
	private PathBuilder pathBuilder;
	private DataClient dataClient;

	public MessageReceiverFactoryImp() {
		imageAnalyzerFactory = new ImageAnalyzerFactoryImp();
		resourceMetadataCreator = new ResourceMetadataCreatorImp();
	}

	@Override
	public MessageReceiver factor(String queueName,
			JavaClientAppTokenCredentials appTokenCredentials, String archiveBasePath,
			String fileStorageBasePath) {
		initializeDataClientAndPathBuilder(appTokenCredentials, archiveBasePath,
				fileStorageBasePath);
		return factorMessageReceiverUsingQueueName(queueName);
	}

	private void initializeDataClientAndPathBuilder(
			JavaClientAppTokenCredentials appTokenCredentials, String archiveBasePath,
			String fileStorageBasePath) {
		dataClient = JavaClientProvider
				.createDataClientUsingJavaClientAppTokenCredentials(appTokenCredentials);
		pathBuilder = new PathBuilderImp(archiveBasePath, fileStorageBasePath);
	}

	private MessageReceiver factorMessageReceiverUsingQueueName(String queueName) {
		if (isImageConverterQueue(queueName)) {
			return factorAnalyzeAndConvertImageToThumbnails();
		}

		if (isPdfConverterQueue(queueName)) {
			return factorConvertPdfToThumbnails();
		}

		throw BinaryConverterException.withMessage(
				"It could not start any message receiver with the queue name: " + queueName);
	}

	private boolean isImageConverterQueue(String queueName) {
		return "smallConverterQueue".equals(queueName);
	}

	private MessageReceiver factorAnalyzeAndConvertImageToThumbnails() {
		ImageConverterFactory imageConverterFactory = new ImageConverterFactoryImp();
		return new AnalyzeAndConvertImageToThumbnails(dataClient, imageAnalyzerFactory,
				imageConverterFactory, pathBuilder, resourceMetadataCreator);
	}

	private boolean isPdfConverterQueue(String queueName) {
		return "pdfConverterQueue".equals(queueName);
	}

	private MessageReceiver factorConvertPdfToThumbnails() {
		PdfConverterFactory pdfConverterFactory = new PdfConverterFactoryImp();
		return new ConvertPdfToThumbnails(pdfConverterFactory, imageAnalyzerFactory, dataClient,
				resourceMetadataCreator, pathBuilder);
	}
}
