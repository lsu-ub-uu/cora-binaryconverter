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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import se.uu.ub.cora.binaryconverter.image.ImageConverterException;

public class PathBuilderImp implements PathBuilder {

	private String hashAlgorithm = "SHA-256";
	private String archiveBasePath;

	public PathBuilderImp(String archiveBasePath) {
		this.archiveBasePath = archiveBasePath;
	}

	@Override
	public String buildPathToAResourceInArchive(String type, String id, String dataDivider) {
		String sha256Path = generateSha256Path(dataDivider, type, id);
		return buildImagePathFromSha256Path(type, id, sha256Path);
	}

	private String buildImagePathFromSha256Path(String type, String id, String sha256Path) {
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
			throw ImageConverterException.withMessageAndException("Error while analyzing image.",
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

	public String onlyForTestGetArchiveBasePath() {
		return archiveBasePath;
	}

}
