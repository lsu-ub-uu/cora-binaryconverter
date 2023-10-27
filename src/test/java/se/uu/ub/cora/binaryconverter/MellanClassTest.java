package se.uu.ub.cora.binaryconverter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.ImageSmallConverter;
import se.uu.ub.cora.binaryconverter.spy.CoraClientFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.MessageListenerSpy;
import se.uu.ub.cora.javaclient.cora.internal.DataClientImp;

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

		// SPIKE

		listener.MCR.assertParameters("listen", 0);
		ImageSmallConverter smallConverter = (ImageSmallConverter) listener.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("listen", 0, "messageReceiver");

		assertNotNull(smallConverter);

		assertEquals(smallConverter.onlyForTestGetOcflHomePath(),
				"/someOcfl/Home/Path/From/Fedora");

		DataClientImp dataClient = (DataClientImp) smallConverter.onlyForTestGetDataClient();

	}

}
