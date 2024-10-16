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

import se.uu.ub.cora.binaryconverter.messagereceiver.MessageReceiverFactory;
import se.uu.ub.cora.binaryconverter.messagereceiver.MessageReceiverFactoryImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageListenerRoutingInfo;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class BinaryConverter {
	private Logger logger;
	private MessageReceiverFactory messageReceiverFactory;
	private String coraUrl;
	private String appTokenUrl;
	private String loginId;
	private String appToken;
	private String hostName;
	private int port;
	private String virtualHost;
	private String queueName;
	private String ocflHome;
	private String fileStorageBasePath;

	public BinaryConverter() {
		logger = LoggerProvider.getLoggerForClass(BinaryConverter.class);
		messageReceiverFactory = new MessageReceiverFactoryImp();
	}

	public static void main(String[] args) {
		BinaryConverter converter = new BinaryConverter();
		converter.startBinaryConverter(args);
	}

	void startBinaryConverter(String[] args) {
		logger.logInfoUsingMessage("BinaryConverter starting...");
		coraUrl = args[0];
		appTokenUrl = args[1];
		loginId = args[2];
		appToken = args[3];
		hostName = args[4];
		port = Integer.parseInt(args[5]);
		virtualHost = args[6];
		queueName = args[7];
		ocflHome = args[8];
		fileStorageBasePath = args[9];

		JavaClientAppTokenCredentials appTokenCredentials = new JavaClientAppTokenCredentials(
				coraUrl, appTokenUrl, loginId, appToken);

		logCoraClientFactory();

		logMessagingLister();
		MessageReceiver messageReceiver = messageReceiverFactory.factor(queueName,
				appTokenCredentials, ocflHome, fileStorageBasePath);
		MessageListener listener = getMessageListener();
		listener.listen(messageReceiver);

		logAnalyzeAndConverterStarter();
	}

	private MessageListener getMessageListener() {
		MessageRoutingInfo routingInfo = new AmqpMessageListenerRoutingInfo(hostName, port,
				virtualHost, queueName);
		return MessagingProvider.getTopicMessageListener(routingInfo);
	}

	private void logMessagingLister() {
		String logMessagingListener = "Start MessagingListener with hostname: {0}, port: {1}, "
				+ "virtualHost: {2} and queueName: {3}.";
		logger.logInfoUsingMessage(MessageFormat.format(logMessagingListener, hostName,
				String.valueOf(port), virtualHost, queueName));
	}

	private void logCoraClientFactory() {
		String logCoraClientFactory = "Start CoraClientFactory with cora url: {0} and appTokenUrl: {1}.";
		logger.logInfoUsingMessage(
				MessageFormat.format(logCoraClientFactory, coraUrl, appTokenUrl));
	}

	private void logAnalyzeAndConverterStarter() {
		String logAnalyzeAndConvertStarter = "Create AnalyzeAndConvertStarter with userId: {0}, "
				+ "appToken: {1} and ocflHome: {2} and fileStorageBasePath: {3}";
		logger.logInfoUsingMessage(MessageFormat.format(logAnalyzeAndConvertStarter, loginId,
				appToken, ocflHome, fileStorageBasePath));
	}

	void onlyForTestSetMessageReceiverFactory(MessageReceiverFactory messageReceiverFactorySpy) {
		messageReceiverFactory = messageReceiverFactorySpy;
	}
}
