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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Map;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.ClientDataResourceLink;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertToThumbnails implements MessageReceiver {

	private static final String IMAGE_JPEG = "image/jpeg";
	ImageAnalyzerFactory imageAnalyzerFactory = new ImageAnalyzerFactoryImp();
	private String archiveBasePath;
	private DataClient dataClient;
	private String hashAlgorithm = "SHA-256";
	private ImageConverterFactory imageConverterFactory;
	private String fileStorageBasePath;

	public AnalyzeAndConvertToThumbnails(DataClient dataClient, String archiveBasePath,
			String fileStorageBasePath, ImageConverterFactory imageConverterFactory) {
		this.dataClient = dataClient;
		this.archiveBasePath = archiveBasePath;
		this.fileStorageBasePath = fileStorageBasePath;
		this.imageConverterFactory = imageConverterFactory;
	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {
		String recordType = headers.get("type");
		String recordId = headers.get("id");
		String dataDivider = headers.get("dataDivider");
		String originalImagePath = buildImagePath(headers);

		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);
		ClientDataGroup resourceInfoGroup = binaryRecordGroup
				.getFirstGroupWithNameInData("resourceInfo");

		analyzeAndUpdateMetadataForMasterRepresentation(originalImagePath, resourceInfoGroup);

		convertAndCreateMetadataForRepresentations(recordId, dataDivider, resourceInfoGroup,
				originalImagePath);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private void analyzeAndUpdateMetadataForMasterRepresentation(String originalImagePath,
			ClientDataGroup resourceInfoGroup) {
		ImageData masterImageData = analyzeImage(originalImagePath);
		updateMasterGroupFromResourceInfo(resourceInfoGroup, masterImageData);
	}

	private void convertAndCreateMetadataForRepresentations(String recordId, String dataDivider,
			ClientDataGroup resourceInfoGroup, String inputPath) {
		String fileStoragePathToAResourceId = buildFileStoragePathToAResourceId(recordId,
				dataDivider);
		String largeRepresentationPath = fileStoragePathToAResourceId + "-large";

		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId, inputPath,
				fileStoragePathToAResourceId, "large", 600);
		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId,
				largeRepresentationPath, fileStoragePathToAResourceId, "medium", 300);
		convertImageUsingResourceTypeNameAndWidth(resourceInfoGroup, recordId,
				largeRepresentationPath, fileStoragePathToAResourceId, "thumbnail", 100);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private String buildFileStoragePathToAResourceId(String recordId, String dataDivider) {
		return fileStorageBasePath + "streams/" + dataDivider + "/" + recordId;
	}

	private void convertImageUsingResourceTypeNameAndWidth(ClientDataGroup resourceInfoGroup,
			String recordId, String pathToImage, String outputPath, String resourceTypeName,
			int convertToWidth) {

		ImageConverter imageConverter = imageConverterFactory.factor();
		imageConverter.convertUsingWidth(pathToImage, outputPath + "-" + resourceTypeName,
				convertToWidth);

		ImageData imageData = analyzeImage(outputPath + "-" + resourceTypeName);

		createMetadataForResourceTypeName(resourceTypeName, resourceInfoGroup, recordId, imageData);
	}

	private void createMetadataForResourceTypeName(String resourceTypeName,
			ClientDataGroup resourceInfoGroup, String recordId, ImageData imageData) {
		ClientDataGroup thumbnailGroup = ClientDataProvider
				.createGroupUsingNameInData(resourceTypeName);

		ClientDataAtomic id = ClientDataProvider.createAtomicUsingNameInDataAndValue("resourceId",
				recordId + "-" + resourceTypeName);
		ClientDataResourceLink resourceLink = ClientDataProvider
				.createResourceLinkUsingNameInDataAndMimeType(resourceTypeName, IMAGE_JPEG);
		ClientDataAtomic fileSize = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("fileSize", imageData.size());
		ClientDataAtomic mimeType = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("mimeType", IMAGE_JPEG);
		ClientDataAtomic height = ClientDataProvider.createAtomicUsingNameInDataAndValue("height",
				imageData.height());
		ClientDataAtomic width = ClientDataProvider.createAtomicUsingNameInDataAndValue("width",
				imageData.width());
		ClientDataAtomic resolution = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("resolution", imageData.resolution());

		thumbnailGroup.addChild(id);
		thumbnailGroup.addChild(resourceLink);
		thumbnailGroup.addChild(fileSize);
		thumbnailGroup.addChild(mimeType);
		thumbnailGroup.addChild(height);
		thumbnailGroup.addChild(width);
		thumbnailGroup.addChild(resolution);

		resourceInfoGroup.addChild(thumbnailGroup);
	}

	private String buildImagePath(Map<String, String> headers) {
		String dataDivider = headers.get("dataDivider");
		String type = headers.get("type");
		String id = headers.get("id");

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

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = imageAnalyzerFactory.factor(pathToImage);
		return analyzer.analyze();
	}

	private void updateMasterGroupFromResourceInfo(ClientDataGroup resourceInfoGroup,
			ImageData imageData) {

		ClientDataGroup masterGroup = resourceInfoGroup.getFirstGroupWithNameInData("master");

		ClientDataAtomic atomicHeight = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("height", imageData.height());
		ClientDataAtomic atomicWidth = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("width", imageData.width());
		ClientDataAtomic atomicResolution = ClientDataProvider
				.createAtomicUsingNameInDataAndValue("resolution", imageData.resolution());

		masterGroup.addChild(atomicHeight);
		masterGroup.addChild(atomicWidth);
		masterGroup.addChild(atomicResolution);
	}

	@Override
	public void topicClosed() {
		// TODO Auto-generated method stub
	}

	public void onlyForTestSetImageAnalyzerFactory(ImageAnalyzerFactory imageAnalyzerFactory) {
		this.imageAnalyzerFactory = imageAnalyzerFactory;
	}

	public void onlyForTestSetHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	public ImageAnalyzerFactory onlyForTestGetImageAnalyzerFactory() {
		return imageAnalyzerFactory;
	}

	public String onlyForTestGetOcflHomePath() {
		return archiveBasePath;
	}

	public DataClient onlyForTestGetDataClient() {
		return dataClient;
	}

	public String onlyForTestGetFileStorageBasePath() {
		return fileStorageBasePath;
	}

	public ImageConverterFactory onlyForTestGetImageConverterFactory() {
		return imageConverterFactory;
	}

}
