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

public interface ImageAnalyzerFactory {

	/**
	 * Factor method creates a new ImageAnalyzer using a path to an image.
	 * 
	 * @param path
	 *            is a String with the absolute path to an image to analyze.
	 * @return an {@link ImageAnalyzer} is an object that can be use to analyze pictures.
	 */
	public ImageAnalyzer factor(String path);

}