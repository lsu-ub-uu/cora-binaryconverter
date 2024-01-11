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
package se.uu.ub.cora.binaryconverter.image;

import se.uu.ub.cora.binaryconverter.internal.BinaryConverterException;

/**
 * ImageConverter is intended to be used to convert images to other formats.
 * <p>
 * Implementations of this interface are not thread safe.
 *
 */
public interface ImageConverter {

	/**
	 * convertAndResizeUsingWidth converts an image to a jpg image. The supplied width is used and
	 * height is relative to the original.
	 * 
	 * @param inputPath
	 *            Path to the file to convert from.
	 * @param outputPath
	 *            Path where to store the converted file.
	 * @param width
	 *            An int with wanted width in pixels
	 * 
	 * @throws BinaryConverterException
	 *             if the conversion fails
	 */

	void convertAndResizeUsingWidth(String inputPath, String outputPath, int width);

	/**
	 * convertToTiff converts an image to TIFF.
	 * 
	 * @param inputPath
	 *            A String with the path of the image to be converted.
	 * @param outputPath
	 *            A string where the TIFF image is stored.
	 */
	void convertToTiff(String inputPath, String outputPath);
}
