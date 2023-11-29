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
package se.uu.ub.cora.binaryconverter.common;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageConverterException;

public class PathBuilderTest {

	private static final String SOME_ARCHIVE_PATH = "someArchivePath";
	private static final String SOME_DATA_DIVIDER = "someDataDivider";
	private static final String SOME_TYPE = "someType";
	private static final String SOME_ID = "someId";
	private PathBuilderImp pathBuilder;

	private static final String SHA256_OF_ID = "d8c88703e3133e12b4f9df4ec1df465a86af0e3a"
			+ "10710fb18db1f55f9ed40622";
	private static final String EXPECTED_ARCHIVE_BASE_MASTER = SOME_ARCHIVE_PATH + "/d8c/887/03e/"
			+ SHA256_OF_ID + "/v1/content/" + "someType:someId-master";

	@BeforeMethod
	private void beforeMethod() {
		pathBuilder = new PathBuilderImp(SOME_ARCHIVE_PATH);
	}

	@Test
	public void testPathBuilderImpImplementsPathBuilder() throws Exception {
		assertTrue(pathBuilder instanceof PathBuilder);
	}

	@Test
	public void testCallBuildPathToAResourceInArchive() throws Exception {

		String path = pathBuilder.buildPathToAResourceInArchive(SOME_TYPE, SOME_ID,
				SOME_DATA_DIVIDER);

		assertEquals(path, EXPECTED_ARCHIVE_BASE_MASTER);
	}

	@Test
	public void testWrongAlgorithm() throws Exception {
		pathBuilder.onlyForTestSetHashAlgorithm("NonExistingAlgorithm");

		try {
			pathBuilder.buildPathToAResourceInArchive(SOME_TYPE, SOME_ID, SOME_DATA_DIVIDER);

			fail("It should fail");
		} catch (Exception e) {
			assertTrue(e instanceof ImageConverterException);
			assertEquals(e.getMessage(), "Error while analyzing image.");
			assertEquals(e.getCause().getMessage(),
					"NonExistingAlgorithm MessageDigest not available");
		}
	}
}
