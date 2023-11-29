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

/**
 * ImageAnalyzer is intended to be used to extract metadata from an image.
 * <p>
 * Implementations of this interface are not thread safe.
 *
 */
public interface ImageAnalyzer {

	/**
	 * Analyze method extracts following metadata from an image:
	 * <ul>
	 * <li>height in pixels</li>
	 * <li>width in pixels</li>
	 * <li>resolution in dpi</li>
	 * </ul>
	 * 
	 * @return the extracted metadata inside a Record {@link ImageData}
	 */
	ImageData analyze();
}
