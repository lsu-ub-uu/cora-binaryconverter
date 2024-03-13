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
package se.uu.ub.cora.binaryconverter.openjpeg2.adapter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg2.spy.Opj2ProcessBuilderSpy;
import se.uu.ub.cora.binaryconverter.openjpeg2.spy.ProcessSpy;

public class Opj2ProcessRunnerTest {

	private static final int TIMEOUT_IN_SECONDS = 1;
	private static final int POLL_SLEEP_TIME_MILLISECONDS = 100;
	private Opj2ProcessBuilderSpy processBuilder;
	private Opj2ProcessRunnerImp processRunner;

	@BeforeMethod
	private void beforeMethod() {
		processBuilder = new Opj2ProcessBuilderSpy();
		processRunner = new Opj2ProcessRunnerImp(processBuilder, POLL_SLEEP_TIME_MILLISECONDS,
				TIMEOUT_IN_SECONDS);
	}

	@Test
	public void testRunOpj2ProcessOK() throws Exception {
		processRunner.runOpj2Process();

		processBuilder.MCR.assertMethodWasCalled("start");
		ProcessSpy process = (ProcessSpy) processBuilder.MCR.getReturnValue("start", 0);

		process.MCR.assertMethodWasCalled("exitValue");
	}

	@Test
	public void testRunOpj2ProcessWaitSeveralLoops_OK() throws Exception {
		int callsToExitValue = 2;
		ProcessSpy processSpy = createProcessSpyWithExitValueSupplier(callsToExitValue);
		processBuilder.MRV.setDefaultReturnValuesSupplier("start", () -> processSpy);

		long timeBeforeTest = System.currentTimeMillis();

		processRunner.runOpj2Process();

		assertLoopsWaitingForSuccesfulCall(callsToExitValue, processSpy, timeBeforeTest);
	}

	private void assertWaitingTimeForExitValueSuccessfulCall(long timeBeforeTest,
			long timeAfterTest, int callsToExitValue) {
		int callsBeforeSuccesful = callsToExitValue - 1;
		int minimalExpectedWaitTime = POLL_SLEEP_TIME_MILLISECONDS * callsBeforeSuccesful;
		long diferenceAfterAndBeforeTest = timeAfterTest - timeBeforeTest;
		assertTrue(diferenceAfterAndBeforeTest >= minimalExpectedWaitTime);
	}

	private ProcessSpy createProcessSpyWithExitValueSupplier(int callsToExitValue) {
		ProcessSpy processSpy = new ProcessSpy();
		Supplier<?> supplier = createExitValueSupplier(callsToExitValue);
		processSpy.MRV.setDefaultReturnValuesSupplier("exitValue", supplier);
		return processSpy;
	}

	private Supplier<?> createExitValueSupplier(int callsToExitValueUntilSuccess) {
		Supplier<?> supplier = new Supplier<Integer>() {
			int counter = 1;

			@Override
			public Integer get() {
				if (callsToExitValueUntilSuccess > counter) {
					counter++;
					throw new IllegalThreadStateException();
				}
				return 0;
			}
		};
		return supplier;
	}

	@Test
	public void testRunOpj2ProcessWaitSeveralLoops_Timeout_ProcessIsAliveS() throws Exception {
		int callsToExitValue = 10;
		ProcessSpy processSpy = new ProcessSpy();
		processSpy.MRV.setAlwaysThrowException("exitValue", new IllegalThreadStateException());
		processBuilder.MRV.setDefaultReturnValuesSupplier("start", () -> processSpy);

		long timeBeforeTest = 0L;
		try {
			timeBeforeTest = System.currentTimeMillis();

			processRunner.runOpj2Process();
			fail();
		} catch (Exception e) {
			assertLoopsWaitingForSuccesfulCall(callsToExitValue, processSpy, timeBeforeTest);

			assertException(e);

			processSpy.MCR.assertMethodWasCalled("destroy");
			processSpy.MCR.assertMethodWasCalled("isAlive");
			processSpy.MCR.assertMethodWasCalled("destroyForcibly");
		}
	}

	private void assertException(Exception e) {
		assertTrue(e instanceof OpenJpeg2Exception);
		assertEquals(e.getMessage(), "Converting image using openjpeg2 failed or timed out");
	}

	private void assertLoopsWaitingForSuccesfulCall(int callsToExitValue, ProcessSpy processSpy,
			long timeBeforeTest) {
		long timeAfterTest = System.currentTimeMillis();
		processSpy.MCR.assertNumberOfCallsToMethod("exitValue", callsToExitValue);
		assertWaitingTimeForExitValueSuccessfulCall(timeBeforeTest, timeAfterTest,
				callsToExitValue);
	}

	@Test
	public void testRunOpj2ProcessWaitSeveralLoops_Timeout_ProcessNotAlive() throws Exception {
		int callsToExitValue = 10;
		ProcessSpy processSpy = new ProcessSpy();
		processSpy.MRV.setAlwaysThrowException("exitValue", new IllegalThreadStateException());
		processSpy.MRV.setDefaultReturnValuesSupplier("isAlive", () -> false);
		processBuilder.MRV.setDefaultReturnValuesSupplier("start", () -> processSpy);

		long timeBeforeTest = 0L;
		try {
			timeBeforeTest = System.currentTimeMillis();

			processRunner.runOpj2Process();
			fail();
		} catch (Exception e) {

			assertLoopsWaitingForUnsuccesfulCall(callsToExitValue, processSpy, timeBeforeTest);

			assertException(e);

			processSpy.MCR.assertMethodWasCalled("destroy");
			processSpy.MCR.assertMethodWasCalled("isAlive");
			processSpy.MCR.assertMethodNotCalled("destroyForcibly");
		}
	}

	private void assertLoopsWaitingForUnsuccesfulCall(int callsToExitValue, ProcessSpy processSpy,
			long timeBeforeTest) {
		long timeAfterTest = System.currentTimeMillis();
		processSpy.MCR.assertNumberOfCallsToMethod("exitValue", callsToExitValue);
		int noSleepsBeforeDestroy = 1;
		assertWaitingTimeForExitValueSuccessfulCall(timeBeforeTest, timeAfterTest,
				callsToExitValue + noSleepsBeforeDestroy);
	}

	@Test
	public void testrunOpj2Process_InterrumpedException() throws Exception {
		int callsToExitValue = 2;
		ProcessSpy processSpy = createProcessSpyWithExitValueSupplier(callsToExitValue);
		processBuilder.MRV.setDefaultReturnValuesSupplier("start", () -> processSpy);

		Opj2ProcessRunner runOpj2Process = new Opj2ProcessRunnerImpOnlyForTest(processBuilder,
				POLL_SLEEP_TIME_MILLISECONDS, TIMEOUT_IN_SECONDS);
		try {
			runOpj2Process.runOpj2Process();
			fail("It should throw exception");
		} catch (Exception e) {
			assertTrue(Thread.currentThread().isInterrupted());

			assertException(e);
			assertEquals(e.getCause(), "someInterruptedException");
		}
	}

	class Opj2ProcessRunnerImpOnlyForTest extends Opj2ProcessRunnerImp {
		public Opj2ProcessRunnerImpOnlyForTest(Opj2ProcessBuilder builder,
				int pollSleepTimeInMillisecond, int timeoutInSeconds) {
			super(builder, pollSleepTimeInMillisecond, timeoutInSeconds);
		}

		@Override
		protected void threadSleep() throws InterruptedException {
			throw new InterruptedException("someInterruptedException");
		}
	}

	@Test
	public void testOnlyForTestMethods() throws Exception {
		assertEquals(processRunner.onlyForTestGetProcessBuilder(), processBuilder);
		assertEquals(processRunner.onlyForTestGetPollSleepTimeInMilliseconds(),
				POLL_SLEEP_TIME_MILLISECONDS);
		assertEquals(processRunner.onlyForTestGetTimeoutInSeconds(), TIMEOUT_IN_SECONDS);
	}
}
