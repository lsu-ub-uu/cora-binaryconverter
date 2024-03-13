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
package se.uu.ub.cora.binaryconverter.openjpeg2;

import java.util.EnumSet;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.OpenJpeg2Exception;

public enum Opj2MimeType {
	BMP("image/bmp", ".bmp"), GRAYMAP("image/x-portable-graymap", ".pgm"), PNG("image/png",
			".png"), ANYMAP("image/x-portable-anymap", ".pnm"), PIXMAP("image/x-portable-pixmap",
					".ppm"), TGA("image/x-tga", ".tga"), TIFF("image/tiff", ".tif");

	private final String mimeType;
	private final String extension;

	Opj2MimeType(String mimeType, String extension) {
		this.mimeType = mimeType;
		this.extension = extension;
	}

	private String getMimeType() {
		return mimeType;
	}

	private String getExtension() {
		return extension;
	}

	private boolean mimeTypeMatches(String mimeType) {
		return getMimeType().equalsIgnoreCase(mimeType);
	}

	public static boolean isAcceptedForOpenJpeg2(String mimeType) {
		return EnumSet.allOf(Opj2MimeType.class).stream().map(Opj2MimeType::getMimeType)
				.anyMatch(mimeType::equalsIgnoreCase);
	}

	public static String getExtensionForMimeType(String mimeType) {
		for (Opj2MimeType opj2MimeType : values()) {
			if (opj2MimeType.mimeTypeMatches(mimeType)) {
				return opj2MimeType.getExtension();
			}
		}
		throw OpenJpeg2Exception.withMessage(
				"Could not match any extension beacause mimeType " + mimeType + " do not match.");
	}
}
