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
package se.uu.ub.cora.binaryconverter;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageBigConverter;
import se.uu.ub.cora.binaryconverter.imageconverter.ImageSmallConverter;
import se.uu.ub.cora.messaging.AmqpMessageListenerRoutingInfo;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class BinaryConverterStarter {

	public static void main(String[] args) {
		// HOST, PORT, QUEUNAME
		// SPIKE STARTS
		String hostName = args[0];
		int port = Integer.parseInt(args[1]);
		String virtualHost = "/";
		String queuName = args[2];
		MessageRoutingInfo routingInfo = new AmqpMessageListenerRoutingInfo(hostName, port,
				virtualHost, queuName);
		MessageListener listener = MessagingProvider.getTopicMessageListener(routingInfo);
		MessageReceiver messageReceiver = createReceiver(queuName);
		listener.listen(messageReceiver);

		// SPIKE ENDS
	}

	private static MessageReceiver createReceiver(String queuName) {
		if (queuName.equals("smallConverterQueue")) {
			return new ImageSmallConverter("OCFL_HOME");
			// new ImageSmallConverter();
		}
		// if (queuName.equals("bigConverterQueue")) {
		return new ImageBigConverter();
		// }
	}

}
