package se.uu.ub.cora.binaryconverter.imageconverter;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.imageconverter.spy.ImageMagickAdapterSpy;
import se.uu.ub.cora.messaging.MessageReceiver;

public class ImageSmallConverterTest {

	private static final String SOME_MESSAGE = "someMessage";
	private ImageSmallConverter imageSmallConverter;
	private Map<String, String> some_headers = new HashMap<>();
	private ImageMagickAdapterSpy imageMagick;

	@BeforeMethod
	public void beforeMethod() {
		imageSmallConverter = new ImageSmallConverter();
		imageMagick = new ImageMagickAdapterSpy();
	}

	@Test
	public void testCallRecievMessage() throws Exception {
		assertTrue(imageSmallConverter instanceof MessageReceiver);

		imageSmallConverter.receiveMessage(some_headers, SOME_MESSAGE);

		imageMagick.MCR.assertParameters("analyze", 0, "/somePath");
	}

}
