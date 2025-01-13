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

import java.nio.file.Files;

import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

public interface FilesWrapper {
	/**
	 * createSymbolicLink wraps Files.createSymbolicLink method see {@link Files}
	 * 
	 * @param link
	 *            is the new symbolic link.
	 * @param target
	 *            is the taget of the link.
	 * @throws BinaryConverterException
	 *             if createSymbolicLink cannot be created.
	 */
	void createSymbolicLink(String link, String target);

	/**
	 * move wraps Files.move method see {@link Files}
	 * 
	 * @param target
	 *            is the path to move.
	 * @param newTarget
	 *            is the path to move to.
	 * @throws BinaryConverterException
	 *             if a target cannot be moved to the new target.
	 */
	void move(String target, String newTarget);

	/**
	 * delete wraps the Files.delete method see {@link Files}
	 * 
	 * @param target
	 *            is the path to delete * @throws BinaryConverterException if a target cannot be
	 *            deleted.
	 */
	void delete(String target);

}
