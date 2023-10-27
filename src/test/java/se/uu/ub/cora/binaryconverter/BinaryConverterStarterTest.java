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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageSmallConverter;
import se.uu.ub.cora.binaryconverter.spy.MessageListenerSpy;
import se.uu.ub.cora.binaryconverter.spy.MessagingFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.RestClientFactorySpy;
import se.uu.ub.cora.javaclient.cora.DataClientFactoryImp;
import se.uu.ub.cora.javaclient.cora.internal.DataClientImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.messaging.AmqpMessageListenerRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class BinaryConverterStarterTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private String testedClassName = "BinaryConverterStarter";

	private String[] args;
	private MessagingFactorySpy messagingFactory;
	// private CoraClientFactorySpy coraClientFactory;
	private RestClientFactorySpy restClientFactory;

	@BeforeMethod
	public void setUp() {
		args = new String[] { "someCoraUrl", "someApptokenUrl", "someUserId", "someApptoken",
				"some", "someRabbitMqHost", "12345", "someVirtualHost", "someRabbitMqQueueName",
				"/someOcfl/Home/Path/From/Fedora" };
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		messagingFactory = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactory);

	}

	private void setUpCoraClientFactory() {
		// coraClientFactory = new CoraClientFactorySpy();
		restClientFactory = new RestClientFactorySpy();
		// DataClientFactoryImp.onlyForTestSetRestClientFactory(restClientFactory);
	}

	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<BinaryConverterStarter> constructor = BinaryConverterStarter.class
				.getDeclaredConstructor();
		assertFalse(constructorIsPublic(constructor));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	private boolean constructorIsPublic(Constructor<BinaryConverterStarter> constructor) {
		return Modifier.isPublic(constructor.getModifiers());
	}

	@Test
	public void testDefaultDataClientFactory() throws Exception {
		BinaryConverterStarter.main(args);

		DataClientFactoryImp dataClientFactory = BinaryConverterStarter
				.onlyForTestGetDataClientFactory();

		assertEquals(dataClientFactory.onlyForTestGetAppTokenVerifierUrl(), "someApptokenUrl");
		assertEquals(dataClientFactory.onlyForTestGetBaseUrl(), "someCoraUrl");
	}

	@Test
	public void testCallBinaryConverterStarter() throws Exception {
		setUpCoraClientFactory();

		BinaryConverterStarter.main(args);

		AmqpMessageListenerRoutingInfo routingInfo = (AmqpMessageListenerRoutingInfo) messagingFactory.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("factorTopicMessageListener", 0,
						"messagingRoutingInfo");

		assertEquals(routingInfo.hostname, "someRabbitMqHost");
		assertEquals(routingInfo.port, 12345);
		assertEquals(routingInfo.virtualHost, "someVirtualHost");
		assertEquals(routingInfo.queueName, "someRabbitMqQueueName");

		coraClientFactory.MCR.assertParameters("factorUsingUserIdAndAppToken", 0, "someUserId",
				"someApptoken");

		MessageListenerSpy listener = (MessageListenerSpy) messagingFactory.MCR
				.getReturnValue("factorTopicMessageListener", 0);

		listener.MCR.assertParameters("listen", 0);
		ImageSmallConverter smallConverter = (ImageSmallConverter) listener.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("listen", 0, "messageReceiver");

		assertNotNull(smallConverter);

		assertEquals(smallConverter.onlyForTestGetOcflHomePath(),
				"/someOcfl/Home/Path/From/Fedora");

		DataClientImp dataClient = (DataClientImp) smallConverter.onlyForTestGetDataClient();

	}

}
