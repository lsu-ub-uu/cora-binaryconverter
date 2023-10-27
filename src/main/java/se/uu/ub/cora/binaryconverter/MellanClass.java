package se.uu.ub.cora.binaryconverter;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageBigConverter;
import se.uu.ub.cora.binaryconverter.imageconverter.ImageSmallConverter;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.javaclient.cora.DataClient;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;

public class MellanClass {

	private CoraClientFactory coraClientFactory;
	private MessageListener listener;
	private String userId;
	private String appToken;
	private String ocflHome;

	public MellanClass(CoraClientFactory coraClientFactory, MessageListener listener, String userId,
			String appToken, String ocflHome) {
		this.coraClientFactory = coraClientFactory;
		this.listener = listener;
		this.userId = userId;
		this.appToken = appToken;
		this.ocflHome = ocflHome;

	}

	public void listen() {
		// SPIKE STARTS HERE
		DataClient dataClient = coraClientFactory.factorUsingUserIdAndAppToken(userId, appToken);

		ImageSmallConverter messageReceiver = new ImageSmallConverter(dataClient, ocflHome);
		listener.listen(messageReceiver);
		// SPIKE ends here

	}

	// SPIKE
	private static MessageReceiver createReceiver(String queueName) {
		if (queueName.equals("smallConverterQueue")) {
			return new ImageSmallConverter(null, "OCFL_HOME");
			// new ImageSmallConverter();
		}
		// if (queuName.equals("bigConverterQueue")) {
		return new ImageBigConverter();
		// }
	}

}
