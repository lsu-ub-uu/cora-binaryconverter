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
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

	private static final String SOME_RABBIT_MQ_QUEUE_NAME = "someRabbitMqQueueName";
	private static final String SOME_RABBIT_VIRTUAL_HOST = "someVirtualHost";
	private static final String SOME_RABBIT_MQ_PORT = "12345";
	private static final String SOME_RABBIT_MQ_HOST = "someRabbitMqHost";
	private static final String SOME_APPTOKEN_URL = "someApptokenUrl";
	private static final String SOME_CORA_URL = "someCoraUrl";
	private static final String SOME_USER_ID = "someUserId";
	private static final String SOME_APPTOKEN = "someAppToken";
	private static final String SOME_OCFL_HOME = "/someOcfl/Home/Path/From/Fedora";
	private static final String SOME_FILE_STORAGE_BASE_PATH = "/someOutputPath/";
	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();

	private String[] args;
	private MessagingFactorySpy messagingFactory;
	// private NotMessageReceiverFacSpy analyzeAndConvertStarterFactory;

	@BeforeMethod
	public void setUp() {
		args = new String[] { SOME_CORA_URL, SOME_APPTOKEN_URL, SOME_USER_ID, SOME_APPTOKEN,
				SOME_RABBIT_MQ_HOST, SOME_RABBIT_MQ_PORT, SOME_RABBIT_VIRTUAL_HOST,
				SOME_RABBIT_MQ_QUEUE_NAME, SOME_OCFL_HOME, SOME_FILE_STORAGE_BASE_PATH };

		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		messagingFactory = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactory);
	}

	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<BinaryConverter> constructor = BinaryConverter.class.getDeclaredConstructor();
		assertFalse(constructorIsPublic(constructor));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	private boolean constructorIsPublic(Constructor<BinaryConverter> constructor) {
		return Modifier.isPublic(constructor.getModifiers());
	}

	@Test
	public void testCallBinaryConverterStarter() throws Exception {
		BinaryConverter.main(args);

		AmqpMessageListenerRoutingInfo routingInfo = (AmqpMessageListenerRoutingInfo) messagingFactory.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("factorTopicMessageListener", 0,
						"messagingRoutingInfo");

		assertEquals(routingInfo.hostname, SOME_RABBIT_MQ_HOST);
		assertEquals(routingInfo.port, 12345);
		assertEquals(routingInfo.virtualHost, SOME_RABBIT_VIRTUAL_HOST);
		assertEquals(routingInfo.queueName, SOME_RABBIT_MQ_QUEUE_NAME);

	}

	@Test
	public void testStartListening() throws Exception {
		// analyzeAndConvertStarterFactory = new NotMessageReceiverFacSpy();
		// BinaryConverter
		// .onlyForTestSetAnalyzeAndConvertStarterFactory(analyzeAndConvertStarterFactory);

		MessageReceiverFactorySpy messageReceiverFactory = new MessageReceiverFactorySpy();
		BinaryConverter.onlyForTestSetMessageReceiverFactory(messageReceiverFactory);

		BinaryConverter.main(args);

		MessageListenerSpy listener = (MessageListenerSpy) messagingFactory.MCR
				.getReturnValue("factorTopicMessageListener", 0);

		JavaClientAppTokenCredentials appTokenCredentials = new JavaClientAppTokenCredentials(
				SOME_CORA_URL, SOME_APPTOKEN_URL, SOME_USER_ID, SOME_APPTOKEN);

		messageReceiverFactory.MCR.assertParameterAsEqual("factor", 0, "appTokenCredentials",
				appTokenCredentials);
		messageReceiverFactory.MCR.assertParameter("factor", 0, "ocflHome", SOME_OCFL_HOME);
		messageReceiverFactory.MCR.assertParameter("factor", 0, "fileStorageBasePath",
				SOME_FILE_STORAGE_BASE_PATH);

		var messageReceiver = messageReceiverFactory.MCR.getReturnValue("factor", 0);
		listener.MCR.assertParameters("listen", 0, messageReceiver);

		// analyzeAndConvertStarterFactory.MCR.assertParameter("factor", 0, "messageListener",
		// listener);
		// analyzeAndConvertStarterFactory.MCR.assertParameterAsEqual("factor", 0,
		// "appTokenCredentials", appTokenCredentials);
		// analyzeAndConvertStarterFactory.MCR.assertParameter("factor", 0, "ocflHome",
		// SOME_OCFL_HOME);
		// analyzeAndConvertStarterFactory.MCR.assertParameter("factor", 0, "fileStorageBasePath",
		// SOME_FILE_STORAGE_BASE_PATH);
		//
		// MessageReceiverFactorySpy messageReceiverFactory = (MessageReceiverFactorySpy)
		// analyzeAndConvertStarterFactory.MCR
		// .getReturnValue("factor", 0);

	}

	@Test
	public void testLoggerInit() throws Exception {
		assertLoggerForClassName(0, "se.uu.ub.cora.messaging.starter.MessagingModuleStarterImp");
		assertLoggerForClassName(1, "se.uu.ub.cora.messaging.MessagingProvider");

		BinaryConverter.main(args);

		loggerFactorySpy.MCR.assertNumberOfCallsToMethod("factorForClass", 3);
		loggerFactorySpy.MCR.methodWasCalled("factorForClass");
		assertLoggerForClassName(2, "se.uu.ub.cora.binaryconverter.BinaryConverter");
	}

	private void assertLoggerForClassName(int callNumber, String className) {
		Class javaClass = (Class) loggerFactorySpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("factorForClass", callNumber,
						"javaClass");

		assertEquals(javaClass.getName(), className);
	}

	@Test
	public void testLogs() throws Exception {
		BinaryConverter.main(args);

		LoggerSpy logger = (LoggerSpy) loggerFactorySpy.MCR.getReturnValue("factorForClass", 2);

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
