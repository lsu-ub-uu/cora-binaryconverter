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
package se.uu.ub.cora.binaryconverter.messagereceiver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.image.ImageAnalyzerFactory;
import se.uu.ub.cora.binaryconverter.image.ImageData;
import se.uu.ub.cora.binaryconverter.imagemagick.spy.ImageAnalyzerFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageAnalyzerSpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterFactorySpy;
import se.uu.ub.cora.binaryconverter.spy.ImageConverterSpy;
import se.uu.ub.cora.binaryconverter.spy.PathBuilderSpy;
import se.uu.ub.cora.binaryconverter.spy.ResourceMetadataCreatorSpy;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertToThumbnailsTest {

	private static final String SOME_FILE_STORAGE_BASE_PATH = "/tmp/streamStorageOnDiskTempStream/";
	private static final String SOME_DATA_DIVIDER = "someDataDivider";
	private static final String SOME_TYPE = "someType";
	private static final String SOME_ID = "someId";
	private static final String SOME_MESSAGE = "someMessage";
	private static final String FILE_SYSTEM_PATH_FOR_RESOURCE = SOME_FILE_STORAGE_BASE_PATH
			+ "streams/" + SOME_DATA_DIVIDER + "/" + SOME_ID;

	private AnalyzeAndConvertToThumbnails converter;
	private Map<String, String> some_headers = new HashMap<>();
	private ImageAnalyzerFactorySpy imageAnalyzerFactory;
	private DataClientSpy dataClient;
	private ClientDataFactorySpy clientDataFactory;
	private ImageConverterFactorySpy imageConverterFactory;
	private ImageAnalyzerSpy analyzerMaster;
	private ImageAnalyzerSpy analyzerThumbnail;
	private ImageAnalyzerSpy analyzerMedium;
	private ImageAnalyzerSpy analyzerLarge;
	private ImageData imageDataMaster = new ImageData("resMaster", "widthMaster", "heightMaster",
			"sizeMaster");
	private ImageData imageDataThumbnail = new ImageData("resThumbnail", "widthThumbnail",
			"heightThumbnail", "sizeSmall");
	private ImageData imageDataMedium = new ImageData("resMedium", "widthMedium", "heightMedium",
			"sizeMedium");
	private ImageData imageDataLarge = new ImageData("resLarge", "widthLarge", "heightLarge",
			"sizeLarge");
	private PathBuilderSpy pathBuilder;
	private ResourceMetadataCreatorSpy resourceMetadataCreator;

	private String basePath = "/tmp/streamStorageOnDiskTempStream/";

	@BeforeMethod
	public void beforeMethod() throws Exception {
		makeSureBasePathExistsAndIsEmpty();
		setUpImageAnalyzerFactory();
		dataClient = new DataClientSpy();
		imageConverterFactory = new ImageConverterFactorySpy();
		pathBuilder = new PathBuilderSpy();
		resourceMetadataCreator = new ResourceMetadataCreatorSpy();

		converter = new AnalyzeAndConvertToThumbnails(dataClient, SOME_FILE_STORAGE_BASE_PATH,
				imageAnalyzerFactory, imageConverterFactory, pathBuilder, resourceMetadataCreator);

		setMessageHeaders();
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);
	}

	public void makeSureBasePathExistsAndIsEmpty() throws IOException {
		File dir = new File(basePath);
		dir.mkdir();
		deleteFiles(basePath);
	}

	private void deleteFiles(String path) throws IOException {
		Stream<Path> list;
		list = Files.list(Paths.get(path));
		list.forEach(p -> {
			try {
				deleteFile(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		list.close();
	}

	private void deleteFile(Path path) throws IOException {
		if (new File(path.toString()).isDirectory()) {
			deleteFiles(path.toString());
		}
		try {
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterMethod
	public void removeTempFiles() throws IOException {
		if (Files.exists(Paths.get(basePath))) {
			deleteFiles(basePath);
			File dir = new File(basePath);
			dir.delete();
		}
	}

	private void setUpImageAnalyzerFactory() {
		analyzerMaster = new ImageAnalyzerSpy();
		analyzerThumbnail = new ImageAnalyzerSpy();
		analyzerMedium = new ImageAnalyzerSpy();
		analyzerLarge = new ImageAnalyzerSpy();

		analyzerMaster.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataMaster);
		analyzerThumbnail.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataThumbnail);
		analyzerMedium.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataMedium);
		analyzerLarge.MRV.setDefaultReturnValuesSupplier("analyze", () -> imageDataLarge);

		imageAnalyzerFactory = new ImageAnalyzerFactorySpy();

		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerMaster,
				"somePathToArchive");
		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerThumbnail,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-thumbnail");
		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerMedium,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-medium");
		imageAnalyzerFactory.MRV.setSpecificReturnValuesSupplier("factor", () -> analyzerLarge,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-large");
	}

	private void setMessageHeaders() {
		some_headers.put("dataDivider", SOME_DATA_DIVIDER);
		some_headers.put("type", SOME_TYPE);
		some_headers.put("id", SOME_ID);
	}

	@Test
	public void testInitNoPermissionOnPathSentAlongException() throws IOException {
		Exception caughtException = null;
		try {
			removeTempFiles();
			converter = new AnalyzeAndConvertToThumbnails(dataClient, "/root/streamsDOESNOTEXIST",
					imageAnalyzerFactory, imageConverterFactory, pathBuilder,
					resourceMetadataCreator);
			converter.receiveMessage(some_headers, SOME_MESSAGE);
		} catch (Exception e) {
			caughtException = e;
		}
		assertTrue(caughtException.getCause() instanceof AccessDeniedException);
		assertEquals(caughtException.getMessage(), "can not write files to disk: "
				+ "java.nio.file.AccessDeniedException: /root/streamsDOESNOTEXIST/streams");
	}

	@Test
	public void testInitMissingPath() throws IOException {
		converter = new AnalyzeAndConvertToThumbnails(dataClient, SOME_FILE_STORAGE_BASE_PATH,
				imageAnalyzerFactory, imageConverterFactory, pathBuilder, resourceMetadataCreator);
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		assertTrue(
				Files.exists(Paths.get(SOME_FILE_STORAGE_BASE_PATH, "streams", "someDataDivider")));
	}

	@Test
	public void testInitPathMoreThanOnce() throws IOException {
		converter = new AnalyzeAndConvertToThumbnails(dataClient, SOME_FILE_STORAGE_BASE_PATH,
				imageAnalyzerFactory, imageConverterFactory, pathBuilder, resourceMetadataCreator);
		converter.receiveMessage(some_headers, SOME_MESSAGE);
		converter.receiveMessage(some_headers, SOME_MESSAGE);
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		assertTrue(
				Files.exists(Paths.get(SOME_FILE_STORAGE_BASE_PATH, "streams", "someDataDivider")));
	}

	@Test
	public void testImageAnalyzerFactoryInitialized() throws Exception {
		assertTrue(converter instanceof MessageReceiver);
		ImageAnalyzerFactory factory = converter.onlyForTestGetImageAnalyzerFactory();
		assertNotNull(factory);
	}

	@Test
	public void testCallFactoryWithCorrectPath() throws Exception {
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		String resourceMasterPath = (String) pathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		imageAnalyzerFactory.MCR.assertParameters("factor", 0, resourceMasterPath);
	}

	@Test
	public void testCallPathBuilderBuild() throws Exception {

		converter.receiveMessage(some_headers, SOME_MESSAGE);

		pathBuilder.MCR.assertParameters("buildPathToAResourceInArchive", 0, SOME_TYPE, SOME_ID,
				SOME_DATA_DIVIDER);
	}

	@Test
	public void testCallAnlayze() throws Exception {

		converter.receiveMessage(some_headers, SOME_MESSAGE);

		pathBuilder.MCR.assertParameters("buildPathToAResourceInArchive", 0, SOME_TYPE, SOME_ID,
				SOME_DATA_DIVIDER);

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) imageAnalyzerFactory.MCR
				.getReturnValue("factor", 0);

		analyzer.MCR.assertParameters("analyze", 0);
	}

	@Test
	public void testUpdateRecordAfterAnalyzing() throws Exception {
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		dataClient.MCR.assertParameters("read", 0, SOME_TYPE, SOME_ID);

		ClientDataRecordGroupSpy binaryRecordGroup = assertUpdateRecordAfterAnalyze();

		dataClient.MCR.assertParameters("update", 0, SOME_TYPE, SOME_ID, binaryRecordGroup);

	}

	private ClientDataRecordGroupSpy assertUpdateRecordAfterAnalyze() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		binaryRecordGroup.MCR.assertParameters("getFirstGroupWithNameInData", 0, "resourceInfo");

		ImageAnalyzerSpy analyzer = (ImageAnalyzerSpy) imageAnalyzerFactory.MCR
				.getReturnValue("factor", 0);
		ImageData imageData = (ImageData) analyzer.MCR.getReturnValue("analyze", 0);

		resourceMetadataCreator.MCR.assertParameters("updateMasterGroupFromResourceInfo", 0,
				getResourceInfo(), imageData);

		return binaryRecordGroup;
	}

	private ClientDataRecordGroupSpy getBinaryRecordGroup() {
		ClientDataRecordSpy dataRecord = (ClientDataRecordSpy) dataClient.MCR.getReturnValue("read",
				0);
		dataRecord.MCR.assertParameters("getDataRecordGroup", 0);
		ClientDataRecordGroupSpy binaryRecordGroup = (ClientDataRecordGroupSpy) dataRecord.MCR
				.getReturnValue("getDataRecordGroup", 0);
		return binaryRecordGroup;
	}

	@Test
	public void testConvertAndAnalyzeAndUpdateAllRepresentations() throws Exception {
		converter.receiveMessage(some_headers, SOME_MESSAGE);

		String resourceMasterPath = (String) pathBuilder.MCR
				.getReturnValue("buildPathToAResourceInArchive", 0);

		imageAnalyzerFactory.MCR.assertNumberOfCallsToMethod("factor", 4);

		assertAnalyzeAndConvertToRepresentation("large", 600, resourceMasterPath, 0, 1);
		assertAnalyzeAndConvertToRepresentation("medium", 300,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-large", 1, 2);
		assertAnalyzeAndConvertToRepresentation("thumbnail", 100,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-large", 2, 3);

		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 0, "large",
				getResourceInfo(), SOME_ID, imageDataLarge);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 1, "medium",
				getResourceInfo(), SOME_ID, imageDataMedium);
		resourceMetadataCreator.MCR.assertParameters("createMetadataForRepresentation", 2,
				"thumbnail", getResourceInfo(), SOME_ID, imageDataThumbnail);
	}

	private void assertAnalyzeAndConvertToRepresentation(String representation, int width,
			String inputPath, int fImageConverterCallNr, int fAnalyzerCallNr) {

		assertConvertToRepresentation(representation, width, inputPath, fImageConverterCallNr);
		assertAnalyzeRepresentation(representation, fAnalyzerCallNr);
	}

	private void assertConvertToRepresentation(String representation, int width, String inputPath,
			int fImageConverterCallNr) {
		imageConverterFactory.MCR.assertParameters("factor", fImageConverterCallNr);
		ImageConverterSpy imageConverter = (ImageConverterSpy) imageConverterFactory.MCR
				.getReturnValue("factor", fImageConverterCallNr);
		imageConverter.MCR.assertParameters("convertUsingWidth", 0, inputPath,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-" + representation, width);
	}

	private void assertAnalyzeRepresentation(String representation, int fAnalyzerCallNr) {
		imageAnalyzerFactory.MCR.assertParameters("factor", fAnalyzerCallNr,
				FILE_SYSTEM_PATH_FOR_RESOURCE + "-" + representation);
		ImageAnalyzerSpy imageAnalyzer = (ImageAnalyzerSpy) imageAnalyzerFactory.MCR
				.getReturnValue("factor", fAnalyzerCallNr);
		imageAnalyzer.MCR.assertParameters("analyze", 0);
	}

	private ClientDataGroupSpy getResourceInfo() {
		ClientDataRecordGroupSpy binaryRecordGroup = getBinaryRecordGroup();
		return (ClientDataGroupSpy) binaryRecordGroup.MCR
				.getReturnValue("getFirstGroupWithNameInData", 0);
	}

	@Test
	public void testOnlyForTestGet() throws Exception {
		assertEquals(converter.onlyForTestGetDataClient(), dataClient);
		assertEquals(converter.onlyForTestGetFileStorageBasePath(), SOME_FILE_STORAGE_BASE_PATH);
		assertEquals(converter.onlyForTestGetImageAnalyzerFactory(), imageAnalyzerFactory);
		assertEquals(converter.onlyForTestGetImageConverterFactory(), imageConverterFactory);
		assertEquals(converter.onlyForTestGetPathBuilder(), pathBuilder);
		assertEquals(converter.onlyForTestGetResourceMetadataCreator(), resourceMetadataCreator);
	}
}
