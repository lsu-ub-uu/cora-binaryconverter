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
package se.uu.ub.cora.binaryconverter.imageconverter;

/**
 * ImageConverter is intended to be used to convert images to other formats.
 * <p>
 * Implementations of this interface are not thread safe.
 *
 */
public interface ImageConverter {

	/**
	 * convertToThumbnail converts a master image to a small image in jpg format. Fixed width 100px
	 * and height relative to the original.
	 * 
	 */
	void convertToThumbnail();
}
