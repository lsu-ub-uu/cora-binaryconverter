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
package se.uu.ub.cora.binaryconverter.openjpeg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

public class FilesWrapperTest {

	private static final String SOME_TARGET = "/tmp/someTarget";
	private static final String SOME_LINK_TO_CREATE = "/tmp/someLinkToCreate";
	private static final String SOME_NEW_TARGET = "/tmp/someNewTarget";
	private FilesWrapper filesWrapper;

	@BeforeMethod
	private void beforeMethod() {
		filesWrapper = new FilesWrapperImp();
	}

	@Test
	public void testInit() throws Exception {

	}

	@Test
	public void testCreateSymbolicLink() throws Exception {
		Files.createFile(Paths.get(SOME_TARGET));

		filesWrapper.createSymbolicLink(SOME_LINK_TO_CREATE, SOME_TARGET);

		assertTrue(Files.exists(Paths.get(SOME_LINK_TO_CREATE)));

		Files.delete(Paths.get(SOME_LINK_TO_CREATE));
		Files.delete(Paths.get(SOME_TARGET));
	}

	@Test
	public void testExceptionOnCreateSymbolicLink() throws Exception {
		Files.createFile(Paths.get(SOME_LINK_TO_CREATE));

		try {
			filesWrapper.createSymbolicLink(SOME_LINK_TO_CREATE, SOME_TARGET);
			fail("it should throw exception");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertEquals(e.getMessage(),
					MessageFormat.format("Could not create symbolic link {0} and target{1}",
							SOME_LINK_TO_CREATE, SOME_TARGET));
			assertEquals(e.getCause().toString(),
					"java.nio.file.FileAlreadyExistsException: " + SOME_LINK_TO_CREATE);
		} finally {
			Files.delete(Paths.get(SOME_LINK_TO_CREATE));
		}
	}

	@Test
	public void testDeleteFile() throws Exception {
		Files.createFile(Paths.get(SOME_TARGET));

		filesWrapper.delete(SOME_TARGET);

		assertFalse(Files.exists(Paths.get(SOME_TARGET)));
	}

	@Test
	public void testExceptionOnDeleteFile() throws Exception {

		try {
			filesWrapper.delete(SOME_TARGET);
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			assertEquals(e.getMessage(), "Could not delete file " + SOME_TARGET);
			assertEquals(e.getCause().toString(),
					"java.nio.file.NoSuchFileException: " + SOME_TARGET);
		}
	}

	@Test
	public void testMoveFile() throws Exception {
		Files.createFile(Paths.get(SOME_TARGET));

		filesWrapper.move(SOME_TARGET, SOME_NEW_TARGET);

		assertFalse(Files.exists(Paths.get(SOME_TARGET)));
		assertTrue(Files.exists(Paths.get(SOME_NEW_TARGET)));

		Files.delete(Paths.get(SOME_NEW_TARGET));
	}

	@Test
	public void testMoveFileReplaceIfExists() throws Exception {
		Files.createFile(Paths.get(SOME_TARGET));
		Files.createFile(Paths.get(SOME_NEW_TARGET));

		filesWrapper.move(SOME_TARGET, SOME_NEW_TARGET);

		assertFalse(Files.exists(Paths.get(SOME_TARGET)));
		assertTrue(Files.exists(Paths.get(SOME_NEW_TARGET)));

		Files.delete(Paths.get(SOME_NEW_TARGET));
	}

	@Test
	public void testExceptionOnMoveFile() throws Exception {

		try {
			filesWrapper.move(SOME_TARGET, SOME_NEW_TARGET);
		} catch (Exception e) {
			assertTrue(e instanceof BinaryConverterException);
			String errorMessage = "Could not move file from {0} to {1}";
			assertEquals(e.getMessage(),
					MessageFormat.format(errorMessage, SOME_TARGET, SOME_NEW_TARGET));
			assertEquals(e.getCause().toString(),
					"java.nio.file.NoSuchFileException: " + SOME_TARGET);
		}
	}

}
