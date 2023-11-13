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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.JavaClientFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.MessageListenerSpy;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientProvider;

public class AnalyzeAndConvertStarterImpTest {
	private static final String SOME_APP_TOKEN_URL = "someAppTokenUrl";
	private static final String SOME_BASE_URL = "someBaseUrl";
	private static final String SOME_OCFL_HOME_PATH = "/someOcfl/Home/Path/From/Fedora";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_USER_ID = "someUserId";
	private AnalyzeAndConvertStarter starter;
	private JavaClientFactorySpy javaClientFactory;
	private MessageListenerSpy listener;
	private JavaClientAppTokenCredentials appTokenCredentials;

	@BeforeMethod
	public void beforeMethod() {
		javaClientFactory = new JavaClientFactorySpy();
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		appTokenCredentials = new JavaClientAppTokenCredentials(SOME_BASE_URL, SOME_APP_TOKEN_URL,
				SOME_USER_ID, SOME_APP_TOKEN);

		listener = new MessageListenerSpy();
	}

	@Test
	public void testListen() throws Exception {
		starter = new AnalyzeAndConvertStarterImp(listener, appTokenCredentials,
				SOME_OCFL_HOME_PATH);

		starter.listen();

		javaClientFactory.MCR
				.assertMethodWasCalled("factorDataClientUsingJavaClientAppTokenCredentials");

		javaClientFactory.MCR.assertParameters("factorDataClientUsingJavaClientAppTokenCredentials",
				0, appTokenCredentials);

		DataClientSpy dataClientSpyFromFactory = (DataClientSpy) javaClientFactory.MCR
				.getReturnValue("factorDataClientUsingJavaClientAppTokenCredentials", 0);

		AnalyzeAndConvertToThumbnails converter = getCreatedConverterFromListenCall();
		assertConverterStartedWithOcflPathAndDataClient(converter, SOME_OCFL_HOME_PATH,
				dataClientSpyFromFactory);
	}

	private AnalyzeAndConvertToThumbnails getCreatedConverterFromListenCall() {
		return (AnalyzeAndConvertToThumbnails) listener.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("listen", 0, "messageReceiver");
	}

	private void assertConverterStartedWithOcflPathAndDataClient(
			AnalyzeAndConvertToThumbnails converter, String ocflHomePath,
			DataClientSpy dataClientSpyFromFactory) {
		assertEquals(converter.onlyForTestGetDataClient(), dataClientSpyFromFactory);
		assertEquals(converter.onlyForTestGetOcflHomePath(), ocflHomePath);
	}

	@Test
	public void testOnlyForTestGet() throws Exception {
		AnalyzeAndConvertStarterImp starter = new AnalyzeAndConvertStarterImp(listener,
				appTokenCredentials, SOME_OCFL_HOME_PATH);

		assertSame(starter.onlyForTestGetMessageListener(), listener);
		assertSame(starter.onlyForTestGetAppTokenCredentials(), appTokenCredentials);
		assertSame(starter.onlyForTestGetOcflHome(), SOME_OCFL_HOME_PATH);
	}

}
