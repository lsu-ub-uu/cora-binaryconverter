/*
 * Copyright 2024 Uppsala University Library
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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class OpenJpegExceptionTest {
	@Test
	public void testInit() {
		OpenJpegException notFound = OpenJpegException.withMessage("message");

		assertEquals(notFound.getMessage(), "message");
	}

	@Test
	public void testInitWithException() {
		Exception exception = new Exception();
		OpenJpegException notFound = OpenJpegException.withMessageAndException("message",
				exception);

		assertEquals(notFound.getMessage(), "message");
		assertEquals(notFound.getCause(), exception);
	}
}
