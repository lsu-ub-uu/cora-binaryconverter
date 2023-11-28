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
package se.uu.ub.cora.binaryconverter.imageconverter;

import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.messaging.MessageListener;

public interface NotMessageReceiverFac {

	MessageReceiverFactory factor(MessageListener messageListener,
			JavaClientAppTokenCredentials appTokenCredentials, String ocflHome,
			String fileStorageBasePath);

	// /**
	// * factor method create a new MessageReceiver. The MessageReceiver returned depends on the
	// type
	// * of the queue that the system is initialized with.
	// *
	// * @param appTokenCredentials
	// * @param ocflHome
	// * @param fileStorageBasePath
	// * @return
	// */
	// MessageReceiver factor(JavaClientAppTokenCredentials appTokenCredentials, String ocflHome,
	// String fileStorageBasePath);

	// new AnalyzeAndConvertToThumbnails(dataClient, ocflHome, fileStorageBasePath,
	// imageConverterFactory);

}