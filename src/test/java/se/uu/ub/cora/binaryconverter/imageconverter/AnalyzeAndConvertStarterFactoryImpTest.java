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

import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.CoraClientInfo;
import se.uu.ub.cora.binaryconverter.spy.MessageListenerSpy;

public class AnalyzeAndConvertStarterFactoryImpTest {

	private static final String SOME_OCFL_HOME = "/someOcfl/Home/Path/From/Fedora";
	private static final String SOME_APP_TOKEN_URL = "someAppTokenUrl";
	private static final String SOME_BASE_URL = "someBaseUrl";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_USER_ID = "someUserId";

	@Test
	public void testFactorAnalyzeAndConverterStarter() throws Exception {
		CoraClientInfo coraClientInfo = new CoraClientInfo(SOME_BASE_URL, SOME_APP_TOKEN_URL,
				SOME_USER_ID, SOME_APP_TOKEN);
		MessageListenerSpy messageListener = new MessageListenerSpy();

		AnalyzeAndConvertStarterFactoryImp factory = new AnalyzeAndConvertStarterFactoryImp();
		AnalyzeAndConvertStarterImp analyzeAndConvertThumbnail = (AnalyzeAndConvertStarterImp) factory
				.factor(messageListener, coraClientInfo, SOME_OCFL_HOME);

		assertSame(analyzeAndConvertThumbnail.onlyForTestGetMessageListener(), messageListener);
		assertSame(analyzeAndConvertThumbnail.onlyForTestGetCoraClientInfo(), coraClientInfo);
		assertSame(analyzeAndConvertThumbnail.onlyForTestGetOcflHome(), SOME_OCFL_HOME);
	}
}
