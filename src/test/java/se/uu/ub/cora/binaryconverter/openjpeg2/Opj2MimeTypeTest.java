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

package se.uu.ub.cora.binaryconverter.openjpeg2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.OpenJpeg2Exception;

public class Opj2MimeTypeTest {

	@Test
	public void testIsAcceptedForOpenJpeg2_NotAccepted() throws Exception {
		assertFalse(Opj2MimeType.isAcceptedForOpenJpeg2("someNoneExistingMimeType"));
	}

	@Test
	public void testIsAcceptedForOpenJpeg2_Accepted() throws Exception {
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/bmp"));
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/x-portable-graymap"));
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/png"));
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/x-portable-anymap"));
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/x-portable-pixmap"));
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/x-raw-panasonic"));
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/x-tga"));
		assertTrue(Opj2MimeType.isAcceptedForOpenJpeg2("image/tiff"));
	}

	@Test
	public void testGetExtensionForMimeType_NotFoundThrowException() throws Exception {
		try {
			Opj2MimeType.getExtensionForMimeType("someNoneExistingMimeType");
			fail("It should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof OpenJpeg2Exception);
			assertEquals(e.getMessage(),
					"Could not match any extension beacause mimeType someNoneExistingMimeType do not match.");
		}
	}

	@Test
	public void testGetExtensionForMimeType_Found() throws Exception {

		assertEquals(Opj2MimeType.getExtensionForMimeType("image/bmp"), ".bmp");
		assertEquals(Opj2MimeType.getExtensionForMimeType("image/x-portable-graymap"), ".pgm");
		assertEquals(Opj2MimeType.getExtensionForMimeType("image/png"), ".png");
		assertEquals(Opj2MimeType.getExtensionForMimeType("image/x-portable-anymap"), ".pnm");
		assertEquals(Opj2MimeType.getExtensionForMimeType("image/x-portable-pixmap"), ".ppm");
		assertEquals(Opj2MimeType.getExtensionForMimeType("image/x-raw-panasonic"), ".raw");
		assertEquals(Opj2MimeType.getExtensionForMimeType("image/x-tga"), ".tga");
		assertEquals(Opj2MimeType.getExtensionForMimeType("image/tiff"), ".tif");
	}
}
