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

import static org.testng.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Command;
import se.uu.ub.cora.binaryconverter.openjpeg2.adapter.Opj2Parameters;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class Opj2CommandSpy implements Opj2Command {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public Opj2CommandSpy() {
		MCR.useMRV(MRV);
	}

	@Override
	public void compress(Opj2Parameters parameters) {
		MCR.addCall("parameters", parameters);

		assertInputFileWithSymbolicLinkExists(parameters);
		createOuputFile(parameters);
	}

	private void assertInputFileWithSymbolicLinkExists(Opj2Parameters parameters) {
		Path path = Paths.get(parameters.getInputPath());
		if (!Files.exists(path)) {
			fail("InputFile does not exists: " + parameters.getInputPath());
		}
	}

	private void createOuputFile(Opj2Parameters parameters) {
		Path path = Paths.get(parameters.getOutputPath());
		try {
			Files.createFile(path);
		} catch (IOException e) {
			fail("It could not create file: " + parameters.getOutputPath());
		}
	}

	@Override
	public void decompress(Opj2Parameters parameters) {
		MCR.addCall("parameters", parameters);
	}

}
