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

import se.uu.ub.cora.binaryconverter.common.PathBuilder;
import se.uu.ub.cora.binaryconverter.common.PathBuilderImp;
import se.uu.ub.cora.binaryconverter.common.ResourceMetadataCreator;
import se.uu.ub.cora.binaryconverter.common.ResourceMetadataCreatorImp;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactoryImp;
import se.uu.ub.cora.binaryconverter.image.ImageConverterFactory;
import se.uu.ub.cora.binaryconverter.imagemagick.ImageConverterFactoryImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientProvider;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class MessageReceiverFactoryImp implements MessageReceiverFactory {

	private ImageConverterFactory imageConverterFactory;

	public MessageReceiverFactoryImp() {
		this.imageConverterFactory = new ImageConverterFactoryImp();
	}

	@Override
	public MessageReceiver factor(JavaClientAppTokenCredentials appTokenCredentials,
			String ocflHome, String fileStorageBasePath) {
		DataClient dataClient = JavaClientProvider
				.createDataClientUsingJavaClientAppTokenCredentials(appTokenCredentials);

		String queueName = "smallConverterQueue";
		return createMessageReceiver(queueName, dataClient, ocflHome, fileStorageBasePath);
	}

	private MessageReceiver createMessageReceiver(String queueName, DataClient dataClient,
			String ocflHome, String fileStorageBasePath) {
		if (queueName.equals("smallConverterQueue")) {
			ImageAnalyzerFactory imageAnalyzerFactory = new ImageAnalyzerFactoryImp();
			ResourceMetadataCreator resourceMetadataCreator = new ResourceMetadataCreatorImp();
			PathBuilder pathBuilder = new PathBuilderImp(ocflHome);

			return new AnalyzeAndConvertToThumbnails(dataClient, fileStorageBasePath,
					imageAnalyzerFactory, imageConverterFactory, pathBuilder,
					resourceMetadataCreator);

		}
		return new ConvertToJpeg2000();
	}

	// public MessageListener onlyForTestGetMessageListener() {
	// return listener;
	// }

	// public Object onlyForTestGetAppTokenCredentials() {
	// return appTokenCredentials;
	// }
	//
	// public String onlyForTestGetOcflHome() {
	// return ocflHome;
	// }
	//
	// public String onlyForTestGetFileStorageBasePath() {
	// return fileStorageBasePath;
	// }

}
