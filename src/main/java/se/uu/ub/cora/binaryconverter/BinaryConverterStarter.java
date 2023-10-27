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

import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.javaclient.cora.DataClient;
import se.uu.ub.cora.javaclient.cora.DataClientFactoryImp;
import se.uu.ub.cora.messaging.AmqpMessageListenerRoutingInfo;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class BinaryConverterStarter {

	private static CoraClientFactory dataClientFactory;

	BinaryConverterStarter() {
	}

	public static void main(String[] args) {
		// HOST, PORT, QUEUNAME
		// SPIKE STARTS
		String coraUrl = args[0];
		String appTokenUrl = args[1];
		String userId = args[2];
		String appToken = args[3];
		String hostName = args[5];
		int port = Integer.parseInt(args[6]);
		String virtualHost = args[7];
		String queueName = args[8];
		String ocflHome = args[9];

		// new MellanClass(recordSettings)

		MessageRoutingInfo routingInfo = new AmqpMessageListenerRoutingInfo(hostName, port,
				virtualHost, queueName);
		MessageListener listener = MessagingProvider.getTopicMessageListener(routingInfo);

		dataClientFactory = DataClientFactoryImp.usingAppTokenVerifierUrlAndBaseUrl(appTokenUrl,
				coraUrl);
		// SPIKE starts here
		MellanClass mc = new MellanClass(dataClientFactory, listener, userId, appToken, ocflHome);
		mc.listen();
		// SPIKE ends here

	}

	static DataClient createDataClient(String userId, String appToken) {
		return dataClientFactory.factorUsingUserIdAndAppToken(userId, appToken);
	}

	public static DataClientFactoryImp onlyForTestGetDataClientFactory() {
		return (DataClientFactoryImp) dataClientFactory;
	}

}
