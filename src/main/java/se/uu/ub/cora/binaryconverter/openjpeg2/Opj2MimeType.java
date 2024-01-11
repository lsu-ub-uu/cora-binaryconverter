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

public enum Opj2MimeType {
	BMP("image/bmp", ".bmp"), GRAYMAP("image/x-portable-graymap", ".pgm"), PNG("image/png",
			".png"), ANYMAP("image/x-portable-anymap", ".pnm"), PIXMAP("image/x-portable-pixmap",
					".ppm"), RAW("image/x-raw-panasonic",
							".raw"), TGA("image/x-tga", ".tga"), TIFF("image/tiff", ".tif");

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

	public static boolean isAcceptedForOpenJpeg2(String mimeType) {
		return EnumSet.allOf(Opj2MimeType.class).stream().map(Opj2MimeType::getMimeType)
				.anyMatch(mimeType::equalsIgnoreCase);
	}

	public static String getExtensionForMimeType(String mimeType) {
		for (Opj2MimeType imageMimeType : values()) {
			if (imageMimeType.getMimeType().equalsIgnoreCase(mimeType)) {
				return imageMimeType.getExtension();
			}
		}
		return ".tif"; // Default extension if not found
	}
}
