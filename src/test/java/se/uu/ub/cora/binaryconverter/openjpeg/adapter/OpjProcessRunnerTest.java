/*
 * Copyright 2023, 2026 Uppsala University Library
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
package se.uu.ub.cora.binaryconverter.openjpeg.adapter;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg.spy.OpjProcessBuilderSpy;
import se.uu.ub.cora.binaryconverter.openjpeg.spy.ProcessSpy;

public class OpjProcessRunnerTest {

	private static final int RETURN_CODE_OK = 0;
	private static final long TIMEOUT_IN_SECONDS = 1;
	private OpjProcessBuilderSpy processBuilder;
	private OpjProcessRunnerImp processRunner;

	@BeforeMethod
	private void beforeMethod() {
		processBuilder = new OpjProcessBuilderSpy();
		processRunner = new OpjProcessRunnerImp(processBuilder, TIMEOUT_IN_SECONDS);
	}

	@Test
	public void testRunOpjProcessOK() {
		ProcessSpy processSpy = setUpProcessSpyWithExitValue(RETURN_CODE_OK);

		processRunner.runOpjProcess();

		processBuilder.MCR.assertMethodWasCalled("start");
		processSpy.MCR.assertCalledParameters("waitFor", TIMEOUT_IN_SECONDS, SECONDS);
	}

	private ProcessSpy setUpProcessSpyWithExitValue(int exitValue) {
		ProcessSpy processSpy = new ProcessSpy();
		processSpy.MRV.setDefaultReturnValuesSupplier("exitValue", () -> exitValue);
		processSpy.MRV.setDefaultReturnValuesSupplier("waitFor", () -> true);
		processBuilder.MRV.setDefaultReturnValuesSupplier("start", () -> processSpy);
		return processSpy;
	}

	@Test
	public void testRunOpjProcessNotOkReturnCode() {
		setUpProcessSpyWithExitValue(666);

		try {
			processRunner.runOpjProcess();
			fail("Openjpeg2 should have been thrown");
		} catch (Exception e) {
			assertTrue(e instanceof OpenJpegException);
			assertEquals(e.getMessage(), "Openjpeg2 conversion failed with exit code 666.");
		}

	}

	@Test
	public void testRunOpjProcessWasInterrupted() {
		InterruptedException interruptedException = new InterruptedException("some error");
		ProcessSpy processSpy = setUpProcessSpyWithExitValue(0);
		setUpProcessToThrowInterruptedExceptionForTimeOut(interruptedException, processSpy,
				TIMEOUT_IN_SECONDS);

		try {
			processRunner.runOpjProcess();
			fail("InterruptedException should have been thrown");
		} catch (Exception e) {
			assertTrue(e instanceof OpenJpegException);
			assertEquals(e.getMessage(),
					"Openjpeg2 conversion was interupted while waiting for conversion to finish.");
			assertSame(e.getCause(), interruptedException);

			processSpy.MCR.assertMethodWasCalled("destroyForcibly");
		}
	}

	private void setUpProcessToThrowInterruptedExceptionForTimeOut(
			InterruptedException interruptedException, ProcessSpy processSpy,
			long timeoutInSeconds) {
		processSpy.MRV.setSpecificReturnValuesSupplier("waitFor", () -> {
			sneakyThrow(interruptedException);
			return null;
		}, timeoutInSeconds, SECONDS);
	}

	public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
		throw (T) t;
	}

	@Test
	public void testRunOpjProcessTimeoutThrowsExceptionAndDestroys() {
		ProcessSpy processSpy = setUpProcessSpyWithExitValue(RETURN_CODE_OK);
		processSpy.MRV.setDefaultReturnValuesSupplier("waitFor", () -> false);
		processSpy.MRV.setDefaultReturnValuesSupplier("isAlive", () -> false);

		try {
			processRunner.runOpjProcess();
			fail("OpenJpegException should have been thrown");
		} catch (Exception e) {
			processSpy.MCR.assertMethodWasCalled("destroy");
			processSpy.MCR.assertMethodNotCalled("destroyForcibly");

			assertTrue(e instanceof OpenJpegException);
			assertEquals(e.getMessage(),
					"Openjpeg2 conversion timed out after " + TIMEOUT_IN_SECONDS + " seconds.");
		}
	}

	@Test
	public void testRunOpjProcessTimeoutDestroysForciblyIfAlive() {
		ProcessSpy processSpy = setUpProcessSpyWithExitValue(RETURN_CODE_OK);
		processSpy.MRV.setDefaultReturnValuesSupplier("waitFor", () -> false);
		processSpy.MRV.setSpecificReturnValuesSupplier("waitFor", () -> true, 15L, SECONDS);
		processSpy.MRV.setDefaultReturnValuesSupplier("isAlive", () -> true);

		try {
			processRunner.runOpjProcess();
			fail("OpenJpegException should have been thrown");
		} catch (Exception e) {
			processSpy.MCR.assertMethodWasCalled("destroy");
			processSpy.MCR.assertMethodWasCalled("destroyForcibly");

			assertTrue(e instanceof OpenJpegException);
			assertEquals(e.getMessage(),
					"Openjpeg2 conversion timed out after " + TIMEOUT_IN_SECONDS + " seconds.");
		}
	}

	@Test
	public void testRunOpjProcessTimeoutDestroysForciblyIfInteruptedWhileDestroy() {
		InterruptedException interruptedException = new InterruptedException("some error2n");
		ProcessSpy processSpy = setUpProcessSpyWithExitValue(RETURN_CODE_OK);
		processSpy.MRV.setDefaultReturnValuesSupplier("waitFor", () -> false);
		processSpy.MRV.setDefaultReturnValuesSupplier("isAlive", () -> true);
		setUpProcessToThrowInterruptedExceptionForTimeOut(interruptedException, processSpy, 15L);

		try {
			processRunner.runOpjProcess();
			fail("OpenJpegException should have been thrown");
		} catch (Exception e) {
			processSpy.MCR.assertMethodWasCalled("destroy");
			processSpy.MCR.assertMethodWasCalled("destroyForcibly");
			processSpy.MCR.assertNumberOfCallsToMethod("destroyForcibly", 1);

			assertTrue(e instanceof OpenJpegException);
			assertEquals(e.getMessage(),
					"Openjpeg2 conversion timed out after " + TIMEOUT_IN_SECONDS + " seconds.");
		}
	}

	@Test
	public void testOnlyForTestMethods() {
		assertEquals(processRunner.onlyForTestGetProcessBuilder(), processBuilder);
		assertEquals(processRunner.onlyForTestGetTimeoutInSeconds(), TIMEOUT_IN_SECONDS);
	}
}
