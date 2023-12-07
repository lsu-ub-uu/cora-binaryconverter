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
package se.uu.ub.cora.binaryconverter.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

public class PathBuilderImp implements PathBuilder {

	private static final String STREAMS_DIR = "streams";
	private String hashAlgorithm = "SHA-256";
	private static final String CAN_NOT_WRITE_FILES_TO_DISK = "can not write files to disk: ";
	private String archiveBasePath;
	private String fileSystemBasePath;

	public PathBuilderImp(String archiveBasePath, String fileSystemBasePath) {
		this.archiveBasePath = archiveBasePath;
		this.fileSystemBasePath = fileSystemBasePath;
	}

	@Override
	public String buildPathToAResourceInArchive(String dataDivider, String type, String id) {
		String sha256Path = generateSha256Path(dataDivider, type, id);
		return buildImagePathFromSha256Path(archiveBasePath, type, id, sha256Path);
	}

	private String buildImagePathFromSha256Path(String archiveBasePath, String type, String id,
			String sha256Path) {
		String sha256PathLowerCase = sha256Path.toLowerCase();
		String folder1 = sha256PathLowerCase.substring(0, 3);
		String folder2 = sha256PathLowerCase.substring(3, 6);
		String folder3 = sha256PathLowerCase.substring(6, 9);
		String folder4 = sha256PathLowerCase;

		return archiveBasePath + "/" + folder1 + "/" + folder2 + "/" + folder3 + "/" + folder4
				+ "/v1/content/" + type + ":" + id + "-master";
	}

	private String generateSha256Path(String dataDivider, String type, String id) {
		String objectIdentifier = generateObjectIdentifier(dataDivider, type, id);
		return generateSha256(objectIdentifier);
	}

	private String generateObjectIdentifier(String dataDivider, String type, String id) {
		String ocflPathLayout = "info:fedora/{0}/resource/{1}:{2}-master";
		return MessageFormat.format(ocflPathLayout, dataDivider, type, id);
	}

	private String generateSha256(String fedoraId) {
		MessageDigest digest = tryToGetDigestAlgorithm();

		final byte[] hashbytes = digest.digest(fedoraId.getBytes(StandardCharsets.UTF_8));
		return bytesToHex(hashbytes);
	}

	private MessageDigest tryToGetDigestAlgorithm() {
		try {
			return MessageDigest.getInstance(hashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw BinaryConverterException.withMessageAndException("Error while analyzing image.",
					e);
		}
	}

	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		forEach(hash, hexString);
		return hexString.toString();
	}

	private static void forEach(byte[] hash, StringBuilder hexString) {
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
	}

	void onlyForTestSetHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	@Override
	public String buildPathToAFileAndEnsureFolderExists(String dataDivider, String type,
			String id) {

		Path pathByDataDivider = Paths.get(fileSystemBasePath, STREAMS_DIR, dataDivider);
		ensureStorageDirectoryExists(pathByDataDivider);
		return buildFileStoragePathToAFile(pathByDataDivider, type, id);
	}

	private void ensureStorageDirectoryExists(Path pathByDataDivider) {
		if (storageDirectoryDoesNotExist(pathByDataDivider)) {
			tryToCreateStorageDirectory(pathByDataDivider);
		}
	}

	private boolean storageDirectoryDoesNotExist(Path pathByDataDivider) {
		return !Files.exists(pathByDataDivider);
	}

	private void tryToCreateStorageDirectory(Path pathByDataDivider) {
		try {
			Files.createDirectories(pathByDataDivider);
		} catch (IOException e) {
			throw new RuntimeException(CAN_NOT_WRITE_FILES_TO_DISK + e, e);
		}
	}

	private String buildFileStoragePathToAFile(Path path, String type, String id) {
		return path.toString() + "/" + type + ":" + id;
	}

	public String onlyForTestGetArchiveBasePath() {
		return archiveBasePath;
	}

	public String onlyForTestGetFileSystemBasePath() {
		return fileSystemBasePath;
	}
}
