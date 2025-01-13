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

package se.uu.ub.cora.binaryconverter.openjpeg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg.adapter.OpenJpegException;

public class OpjMimeTypeTest {

	@Test
	public void testIsAcceptedForOpenJpeg2_NotAccepted() throws Exception {
		assertFalse(OpjMimeType.isAcceptedForOpenJpeg2("someNoneExistingMimeType"));
	}

	@Test
	public void testIsAcceptedForOpenJpeg2_Accepted() throws Exception {
		assertTrue(OpjMimeType.isAcceptedForOpenJpeg2("image/bmp"));
		assertTrue(OpjMimeType.isAcceptedForOpenJpeg2("image/x-portable-graymap"));
		assertTrue(OpjMimeType.isAcceptedForOpenJpeg2("image/png"));
		assertTrue(OpjMimeType.isAcceptedForOpenJpeg2("image/x-portable-anymap"));
		assertTrue(OpjMimeType.isAcceptedForOpenJpeg2("image/x-portable-pixmap"));
		assertTrue(OpjMimeType.isAcceptedForOpenJpeg2("image/x-tga"));
		assertTrue(OpjMimeType.isAcceptedForOpenJpeg2("image/tiff"));
	}

	@Test
	public void testGetExtensionForMimeType_NotFoundThrowException() throws Exception {
		try {
			OpjMimeType.getExtensionForMimeType("someNoneExistingMimeType");
			fail("It should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof OpenJpegException);
			assertEquals(e.getMessage(),
					"Could not match any extension beacause mimeType someNoneExistingMimeType do not match.");
		}
	}

	@Test
	public void testGetExtensionForMimeType_Found() throws Exception {
		assertEquals(OpjMimeType.getExtensionForMimeType("image/bmp"), ".bmp");
		assertEquals(OpjMimeType.getExtensionForMimeType("image/x-portable-graymap"), ".pgm");
		assertEquals(OpjMimeType.getExtensionForMimeType("image/png"), ".png");
		assertEquals(OpjMimeType.getExtensionForMimeType("image/x-portable-anymap"), ".pnm");
		assertEquals(OpjMimeType.getExtensionForMimeType("image/x-portable-pixmap"), ".ppm");
		assertEquals(OpjMimeType.getExtensionForMimeType("image/x-tga"), ".tga");
		assertEquals(OpjMimeType.getExtensionForMimeType("image/tiff"), ".tif");
	}
}
