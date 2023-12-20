package se.uu.ub.cora.binaryconverter.openjpeg2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Opj2ProcessBuilderTest {

	private Opj2ProcessBuilderImp opj2ProcessBuilder;

	@BeforeMethod
	private void beforeMethod() {
		opj2ProcessBuilder = new Opj2ProcessBuilderImp(List.of("pwd"));
	}

	@Test
	public void testInit() throws Exception {
		assertTrue(opj2ProcessBuilder instanceof Opj2ProcessBuilder);

		ProcessBuilder processBuilder = opj2ProcessBuilder.onlyForTestGetProcessBuilder();
		assertEquals(processBuilder.command().size(), 1);
		assertEquals(processBuilder.command().get(0), "pwd");
	}

	@Test
	public void testStart() throws Exception {

		Process process = opj2ProcessBuilder.start();

		assertNotNull(process);

	}

	@Test
	public void testStartThrowsException() throws Exception {

		opj2ProcessBuilder = new Opj2ProcessBuilderOnlyForTest(List.of("someCommand"));

		try {
			opj2ProcessBuilder.start();
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof OpenJpeg2Exception);
			assertEquals(e.getMessage(), "spyException");
		}

	}

	class Opj2ProcessBuilderOnlyForTest extends Opj2ProcessBuilderImp {

		public Opj2ProcessBuilderOnlyForTest(List<String> list) {
			super(list);
		}

		@Override
		Process runStart() throws IOException {
			throw new IOException("spyException");
		}
	}

	@Test
	public void testInheritIO() throws Exception {

		Opj2ProcessBuilderOnlyForTest2 opj2ProcessBuilder = new Opj2ProcessBuilderOnlyForTest2(
				List.of("pwd"));

		Opj2ProcessBuilderImp inheritIO = (Opj2ProcessBuilderImp) opj2ProcessBuilder.inheritIO();
		opj2ProcessBuilder.start();
		Redirect redirectOutput = opj2ProcessBuilder.redirectOutput();

		assertSame(inheritIO, opj2ProcessBuilder);
		assertEquals(redirectOutput.toString(), "INHERIT");

	}

	class Opj2ProcessBuilderOnlyForTest2 extends Opj2ProcessBuilderImp {

		public Opj2ProcessBuilderOnlyForTest2(List<String> list) {
			super(list);
		}

		Redirect redirectOutput() {
			return processBuilder.redirectOutput();
		}
	}
}
