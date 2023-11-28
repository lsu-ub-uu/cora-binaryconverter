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
package se.uu.ub.cora.binaryconverter;

import java.text.MessageFormat;

import se.uu.ub.cora.binaryconverter.imageconverter.MessageReceiverFactory;
import se.uu.ub.cora.binaryconverter.imageconverter.MessageReceiverFactoryImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageListenerRoutingInfo;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class BinaryConverter {

	private static Logger logger = LoggerProvider.getLoggerForClass(BinaryConverter.class);
	private static MessageReceiverFactory messageReceiverFactory = new MessageReceiverFactoryImp();
	private static String coraUrl;
	private static String appTokenUrl;
	private static String userId;
	private static String appToken;
	private static String hostName;
	private static int port;
	private static String virtualHost;
	private static String queueName;
	private static String ocflHome;
	private static String fileStorageBasePath;

	private static JavaClientAppTokenCredentials appTokenCredentials;

	BinaryConverter() {

	}

	public static void main(String[] args) {
		logger.logInfoUsingMessage("BinaryConverter starting...");
		startBinaryConverter(args);
	}

	private static void startBinaryConverter(String[] args) {
		coraUrl = args[0];
		appTokenUrl = args[1];
		userId = args[2];
		appToken = args[3];
		hostName = args[4];
		port = Integer.parseInt(args[5]);
		virtualHost = args[6];
		queueName = args[7];
		ocflHome = args[8];
		fileStorageBasePath = args[9];

		appTokenCredentials = new JavaClientAppTokenCredentials(coraUrl, appTokenUrl, userId,
				appToken);

		logCoraClientFactory();

		logMessagingLister();
		MessageReceiver messageReceiver = messageReceiverFactory.factor(appTokenCredentials,
				ocflHome, fileStorageBasePath);
		MessageListener listener = getMessageListener();
		listener.listen(messageReceiver);

		logAnalyzeAndConverterStarter();
		// startListeningForConvertMessages(listener);
	}

	private static MessageListener getMessageListener() {
		MessageRoutingInfo routingInfo = new AmqpMessageListenerRoutingInfo(hostName, port,
				virtualHost, queueName);
		return MessagingProvider.getTopicMessageListener(routingInfo);
	}

	// private static void startListeningForConvertMessages(MessageListener listener) {
	//
	// MessageReceiverFactory starter = analyzeAndConvertStarterFactory.factor(listener,
	// appTokenCredentials, ocflHome, fileStorageBasePath);
	// starter.factor();
	// }

	private static void logMessagingLister() {
		String logMessagingListener = "Start MessagingListener with hostname: {0}, port: {1}, "
				+ "virtualHost: {2} and queueName: {3}.";
		logger.logInfoUsingMessage(MessageFormat.format(logMessagingListener, hostName,
				String.valueOf(port), virtualHost, queueName));
	}

	private static void logCoraClientFactory() {
		String logCoraClientFactory = "Start CoraClientFactory with cora url: {0} and appTokenUrl: {1}.";
		logger.logInfoUsingMessage(
				MessageFormat.format(logCoraClientFactory, coraUrl, appTokenUrl));
	}

	private static void logAnalyzeAndConverterStarter() {
		String logAnalyzeAndConvertStarter = "Create AnalyzeAndConvertStarter with userId: {0}, "
				+ "appToken: {1} and ocflHome: {2} and fileStorageBasePath {3}.";
		logger.logInfoUsingMessage(MessageFormat.format(logAnalyzeAndConvertStarter, userId,
				appToken, ocflHome, fileStorageBasePath));
	}

	public static void onlyForTestSetMessageReceiverFactory(
			MessageReceiverFactory messageReceiverFactorySpy) {
		messageReceiverFactory = messageReceiverFactorySpy;
	}

	// public static void onlyForTestSetAnalyzeAndConvertStarterFactory(
	// NotMessageReceiverFac analyzeAndConvertStarterFactorySpy) {
	// analyzeAndConvertStarterFactory = analyzeAndConvertStarterFactorySpy;
	// }

}
