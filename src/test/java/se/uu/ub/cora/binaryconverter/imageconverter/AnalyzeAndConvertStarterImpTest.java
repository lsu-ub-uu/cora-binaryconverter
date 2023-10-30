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
package se.uu.ub.cora.binaryconverter.imageconverter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.spy.CoraClientFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.MessageListenerSpy;

public class AnalyzeAndConvertStarterImpTest {
	private static final String SOME_OCFL_HOME_PATH = "/someOcfl/Home/Path/From/Fedora";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_USER_ID = "someUserId";
	private AnalyzeAndConvertStarter starter;
	private CoraClientFactorySpy dataClientFactory;
	private MessageListenerSpy listener;

	@BeforeMethod
	public void beforeMethod() {
		dataClientFactory = new CoraClientFactorySpy();
		listener = new MessageListenerSpy();
	}

	@Test
	public void testListen() throws Exception {
		starter = new AnalyzeAndConvertStarterImp(dataClientFactory, listener, SOME_USER_ID,
				SOME_APP_TOKEN, SOME_OCFL_HOME_PATH);

		starter.listen();

		assertCoraClientFactoryCalledWithUserAndAppToken(SOME_USER_ID, SOME_APP_TOKEN);
		DataClientSpy dataClientSpyFromFactory = getCreatedDataClient();

		AnalyzeAndConvertToThumbnails converter = getCreatedConverterFromListenCall();
		assertConverterStartedWithOcflPathAndDataClient(converter, SOME_OCFL_HOME_PATH,
				dataClientSpyFromFactory);
	}

	private void assertCoraClientFactoryCalledWithUserAndAppToken(String userId, String appToken) {
		dataClientFactory.MCR.assertParameters("factorUsingUserIdAndAppToken", 0, userId, appToken);
	}

	private DataClientSpy getCreatedDataClient() {
		return (DataClientSpy) dataClientFactory.MCR.getReturnValue("factorUsingUserIdAndAppToken",
				0);
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
}
