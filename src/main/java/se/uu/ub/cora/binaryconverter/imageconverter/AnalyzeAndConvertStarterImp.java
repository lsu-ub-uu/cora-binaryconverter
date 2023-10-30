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

import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.javaclient.cora.DataClient;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertStarterImp implements AnalyzeAndConvertStarter {

	private CoraClientFactory dataClientFactory;
	private MessageListener listener;
	private String userId;
	private String appToken;
	private String ocflHome;

	public AnalyzeAndConvertStarterImp(CoraClientFactory dataClientFactory,
			MessageListener listener, String userId, String appToken, String ocflHome) {
		this.dataClientFactory = dataClientFactory;
		this.listener = listener;
		this.userId = userId;
		this.appToken = appToken;
		this.ocflHome = ocflHome;
	}

	@Override
	public void listen() {
		DataClient dataClient = dataClientFactory.factorUsingUserIdAndAppToken(userId, appToken);

		String queueName = "smallConverterQueue";
		MessageReceiver messageReceiver = createReceiver(queueName, dataClient);
		listener.listen(messageReceiver);
	}

	private MessageReceiver createReceiver(String queueName, DataClient dataClient) {
		if (queueName.equals("smallConverterQueue")) {
			return new AnalyzeAndConvertToThumbnails(dataClient, ocflHome);
		}
		return new ConvertToJpeg2000();
	}

	public CoraClientFactory onlyForTestGetCoraClientFactory() {
		return dataClientFactory;
	}

	public MessageListener onlyForTestGetMessageListener() {
		return listener;
	}

	public String onlyForTestGetUserId() {
		return userId;
	}

	public String onlyForTestGetAppToken() {
		return appToken;
	}

	public String onlyForTestGetOcflHome() {
		return ocflHome;
	}

}
