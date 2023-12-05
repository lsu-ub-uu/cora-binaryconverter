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

import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.messaging.MessageReceiver;

public interface MessageReceiverFactory {

	/**
	 * factor method create a new MessageReceiver. The MessageReceiver returned depends on the type
	 * of the queue that the system is initialized with.
	 * 
	 * @param queueName
	 *            is the name of the queue
	 *
	 * @param appTokenCredentials
	 * @param ocflHome
	 * @param fileStorageBasePath
	 * @return
	 */
	MessageReceiver factor(String queueName, JavaClientAppTokenCredentials appTokenCredentials,
			String ocflHome, String fileStorageBasePath);

}