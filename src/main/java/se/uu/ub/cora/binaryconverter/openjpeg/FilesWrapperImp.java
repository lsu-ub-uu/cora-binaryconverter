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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;

import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

public class FilesWrapperImp implements FilesWrapper {

	@Override
	public void createSymbolicLink(String link, String target) {
		try {
			Files.createSymbolicLink(Paths.get(link), Paths.get(target));
		} catch (IOException e) {
			String errorMessage = "Could not create symbolic link {0} and target{1}";
			throw BinaryConverterException
					.withMessageAndException(MessageFormat.format(errorMessage, link, target), e);
		}
	}

	@Override
	public void delete(String target) {
		try {
			Files.delete(Paths.get(target));
		} catch (Exception e) {
			String errorMessage = "Could not delete file {0}";
			throw BinaryConverterException
					.withMessageAndException(MessageFormat.format(errorMessage, target), e);
		}
	}

	@Override
	public void move(String target, String newTarget) {
		try {
			Files.move(Paths.get(target), Paths.get(newTarget),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			String errorMessage = "Could not move file from {0} to {1}";
			throw BinaryConverterException.withMessageAndException(
					MessageFormat.format(errorMessage, target, newTarget), e);
		}

	}
}
