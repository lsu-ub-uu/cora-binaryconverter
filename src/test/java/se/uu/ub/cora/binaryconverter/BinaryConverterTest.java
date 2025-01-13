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

import static org.testng.Assert.assertEquals;

import java.text.MessageFormat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.messagereceiver.ConvertPdfToThumbnails;
import se.uu.ub.cora.binaryconverter.spy.MessageListenerSpy;
import se.uu.ub.cora.binaryconverter.spy.MessageReceiverFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.MessagingFactorySpy;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.logger.spies.LoggerSpy;
import se.uu.ub.cora.messaging.AmqpMessageListenerRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class BinaryConverterTest {
	private static final String SOME_RABBIT_MQ_QUEUE_NAME = "pdfConverterQueue";
	private static final String SOME_RABBIT_VIRTUAL_HOST = "someVirtualHost";
	private static final String SOME_RABBIT_MQ_PORT = "12345";
	private static final String SOME_RABBIT_MQ_HOST = "someRabbitMqHost";
	private static final String SOME_APPTOKEN_URL = "someApptokenUrl";
	private static final String SOME_CORA_URL = "someCoraUrl";
	private static final String SOME_USER_ID = "someUserId";
	private static final String SOME_APPTOKEN = "someAppToken";
	private static final String SOME_OCFL_HOME = "/someOcfl/Home/Path/From/Fedora";
	private static final String SOME_FILE_STORAGE_BASE_PATH = "/someOutputPath/";
	private LoggerFactorySpy loggerFactorySpy;

	private String[] args;
	private MessagingFactorySpy messagingFactory;

	@BeforeMethod
	public void setUp() {
		messagingFactory = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactory);
		args = new String[] { SOME_CORA_URL, SOME_APPTOKEN_URL, SOME_USER_ID, SOME_APPTOKEN,
				SOME_RABBIT_MQ_HOST, SOME_RABBIT_MQ_PORT, SOME_RABBIT_VIRTUAL_HOST,
				SOME_RABBIT_MQ_QUEUE_NAME, SOME_OCFL_HOME, SOME_FILE_STORAGE_BASE_PATH };

		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
	}

	@Test
	public void testCallBinaryConverterStarter() throws Exception {
		BinaryConverter.main(args);

		AmqpMessageListenerRoutingInfo routingInfo = (AmqpMessageListenerRoutingInfo) messagingFactory.MCR
				.getParameterForMethodAndCallNumberAndParameter("factorTopicMessageListener", 0,
						"messagingRoutingInfo");

		assertEquals(routingInfo.hostname, SOME_RABBIT_MQ_HOST);
		assertEquals(routingInfo.port, 12345);
		assertEquals(routingInfo.virtualHost, SOME_RABBIT_VIRTUAL_HOST);
		assertEquals(routingInfo.queueName, SOME_RABBIT_MQ_QUEUE_NAME);
	}

	@Test
	public void testStartListening() throws Exception {
		BinaryConverter.main(args);

		MessageReceiverFactorySpy messageReceiverFactory = new MessageReceiverFactorySpy();
		BinaryConverter converter = new BinaryConverter();
		converter.onlyForTestSetMessageReceiverFactory(messageReceiverFactory);
		converter.startBinaryConverter(args);

		MessageListenerSpy listener = (MessageListenerSpy) messagingFactory.MCR
				.getReturnValue("factorTopicMessageListener", 1);

		JavaClientAppTokenCredentials appTokenCredentials = new JavaClientAppTokenCredentials(
				SOME_CORA_URL, SOME_APPTOKEN_URL, SOME_USER_ID, SOME_APPTOKEN);

		messageReceiverFactory.MCR.assertParameter("factor", 0, "queueName",
				SOME_RABBIT_MQ_QUEUE_NAME);
		messageReceiverFactory.MCR.assertParameterAsEqual("factor", 0, "appTokenCredentials",
				appTokenCredentials);
		messageReceiverFactory.MCR.assertParameter("factor", 0, "ocflHome", SOME_OCFL_HOME);
		messageReceiverFactory.MCR.assertParameter("factor", 0, "fileStorageBasePath",
				SOME_FILE_STORAGE_BASE_PATH);

		var messageReceiver = messageReceiverFactory.MCR.getReturnValue("factor", 0);
		listener.MCR.assertParameters("listen", 0, messageReceiver);
	}

	@Test
	public void testLoggerInit() throws Exception {
		BinaryConverter.main(args);

		loggerFactorySpy.MCR.assertParameters("factorForClass", 0, BinaryConverter.class);
		loggerFactorySpy.MCR.assertParameters("factorForClass", 1, ConvertPdfToThumbnails.class);
		loggerFactorySpy.MCR.assertNumberOfCallsToMethod("factorForClass", 2);
	}

	@Test
	public void testLogs() throws Exception {
		BinaryConverter.main(args);

		LoggerSpy logger = (LoggerSpy) loggerFactorySpy.MCR.getReturnValue("factorForClass", 0);

		String logMessagingListener = "Start MessagingListener with hostname: {0}, port: {1}, "
				+ "virtualHost: {2} and queueName: {3}.";
		String logCoraClientFactory = "Start CoraClientFactory with cora url: {0} and appTokenUrl: {1}.";
		String logAnalyzeAndConvertStarter = "Create AnalyzeAndConvertStarter with userId: {0}, "
				+ "appToken: {1} and ocflHome: {2} and fileStorageBasePath: {3}";

		logger.MCR.assertParameters("logInfoUsingMessage", 0, "BinaryConverter starting...");
		logger.MCR.assertParameters("logInfoUsingMessage", 1,
				MessageFormat.format(logCoraClientFactory, SOME_CORA_URL, SOME_APPTOKEN_URL));
		logger.MCR.assertParameters("logInfoUsingMessage", 2,
				MessageFormat.format(logMessagingListener, SOME_RABBIT_MQ_HOST, SOME_RABBIT_MQ_PORT,
						SOME_RABBIT_VIRTUAL_HOST, SOME_RABBIT_MQ_QUEUE_NAME));
		logger.MCR.assertParameters("logInfoUsingMessage", 3,
				MessageFormat.format(logAnalyzeAndConvertStarter, SOME_USER_ID, SOME_APPTOKEN,
						SOME_OCFL_HOME, SOME_FILE_STORAGE_BASE_PATH));
	}
}
