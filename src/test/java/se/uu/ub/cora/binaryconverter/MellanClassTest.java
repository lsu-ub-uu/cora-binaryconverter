package se.uu.ub.cora.binaryconverter;

import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.spy.CoraClientFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.MessageListenerSpy;

public class MellanClassTest {

	private MellanClass mc;
	private CoraClientFactorySpy coraClientFactory;

	@Test
	public void testInit() throws Exception {
		coraClientFactory = new CoraClientFactorySpy();
		MessageListenerSpy listener = new MessageListenerSpy();

		mc = new MellanClass(coraClientFactory, listener, "someUserId", "someAppToken",
				"someOcflHome");

		mc.listen();

	}

}
