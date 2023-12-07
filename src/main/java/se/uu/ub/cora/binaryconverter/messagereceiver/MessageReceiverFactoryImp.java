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

import se.uu.ub.cora.binaryconverter.imagemagick.BinaryOperationFactoryImp;
import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;
import se.uu.ub.cora.binaryconverter.internal.BinaryOperationFactory;
import se.uu.ub.cora.binaryconverter.internal.PathBuilder;
import se.uu.ub.cora.binaryconverter.internal.PathBuilderImp;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreator;
import se.uu.ub.cora.binaryconverter.internal.ResourceMetadataCreatorImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientProvider;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class MessageReceiverFactoryImp implements MessageReceiverFactory {

	private BinaryOperationFactory binaryOperationFactory;
	private ResourceMetadataCreator resourceMetadataCreator;
	private PathBuilder pathBuilder;
	private DataClient dataClient;

	public MessageReceiverFactoryImp() {
		binaryOperationFactory = new BinaryOperationFactoryImp();
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
		if (isJp2ConverterQueue(queueName)) {
			return factorConvertImageToJp2();
		}

		throw BinaryConverterException.withMessage(
				"It could not start any message receiver with the queue name: " + queueName);
	}

	private boolean isImageConverterQueue(String queueName) {
		return "smallImageConverterQueue".equals(queueName);
	}

	private MessageReceiver factorAnalyzeAndConvertImageToThumbnails() {

		return new AnalyzeAndConvertImageToThumbnails(dataClient, binaryOperationFactory,
				pathBuilder, resourceMetadataCreator);
	}

	private boolean isPdfConverterQueue(String queueName) {
		return "pdfConverterQueue".equals(queueName);
	}

	private MessageReceiver factorConvertPdfToThumbnails() {
		return new ConvertPdfToThumbnails(binaryOperationFactory, dataClient,
				resourceMetadataCreator, pathBuilder);
	}

	private boolean isJp2ConverterQueue(String queueName) {
		return "jp2ConverterQueue".equals(queueName);
	}

	private MessageReceiver factorConvertImageToJp2() {
		return new ConvertImageToJp2(binaryOperationFactory, dataClient, resourceMetadataCreator,
				pathBuilder);
	}
}
