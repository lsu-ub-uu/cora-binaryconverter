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

import se.uu.ub.cora.binaryconverter.CoraClientInfo;
import se.uu.ub.cora.javaclient.JavaClientProvider;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertStarterImp implements AnalyzeAndConvertStarter {

	private MessageListener listener;
	private String ocflHome;
	private CoraClientInfo coraClientInfo;

	public AnalyzeAndConvertStarterImp(MessageListener listener, CoraClientInfo coraClientInfo,
			String ocflHome) {
		this.listener = listener;
		this.coraClientInfo = coraClientInfo;
		this.ocflHome = ocflHome;
	}

	@Override
	public void listen() {
		DataClient dataClient = JavaClientProvider
				.getDataClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppToken(
						coraClientInfo.baseUrl(), coraClientInfo.appTokenUrl(),
						coraClientInfo.userId(), coraClientInfo.appToken());

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

	public MessageListener onlyForTestGetMessageListener() {
		return listener;
	}

	public Object onlyForTestGetCoraClientInfo() {
		return coraClientInfo;
	}

	public String onlyForTestGetOcflHome() {
		return ocflHome;
	}

}
