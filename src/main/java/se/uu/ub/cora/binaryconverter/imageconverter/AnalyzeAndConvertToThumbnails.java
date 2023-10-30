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

import java.util.Map;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.cora.DataClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class AnalyzeAndConvertToThumbnails implements MessageReceiver {

	ImageAnalyzerFactory imageAnalyzerFactory = new ImageAnalyzerFactoryImp();
	private String ocflHomePath;
	private DataClient dataClient;

	public AnalyzeAndConvertToThumbnails(DataClient dataClient, String ocflHomePath) {
		this.dataClient = dataClient;
		this.ocflHomePath = ocflHomePath;
	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {

		String pathToImage = buildImagePath(headers);
		ImageData imageData = analyzeImage(pathToImage);
		updateRecord(headers, imageData);

		// Read binary record
		// Get checksum256 or checksum512
		// If checksum 000111222333333333333333333
		// Build path {OCFL_ROOT_HOME}/000/111/222/333333333333333333/V_1/content/{type}:{id}-master

		// factory in to factor new analyzers (adapter)

		// create and call analyzer (adapter)
		// Update binary record

		// CONVERT STEP
		// create and call converter for thumbnail
		// create and call converter for small
		// create and call converter for large

		// update binaryRecord call api
	}

	private String buildImagePath(Map<String, String> headers) {
		String cheksum = headers.get("checksum512");
		String type = headers.get("type");
		String id = headers.get("id");

		String checkSumLoweCase = cheksum.toLowerCase();
		String folder1 = checkSumLoweCase.substring(0, 3);
		String folder2 = checkSumLoweCase.substring(3, 6);
		String folder3 = checkSumLoweCase.substring(6, 9);
		String folder4 = checkSumLoweCase.substring(9, checkSumLoweCase.length());

		return ocflHomePath + "/" + folder1 + "/" + folder2 + "/" + folder3 + "/" + folder4
				+ "/v1/content/" + type + ":" + id + "-master";
	}

	private ImageData analyzeImage(String pathToImage) {
		ImageAnalyzer analyzer = imageAnalyzerFactory.factor(pathToImage);
		return analyzer.analyze();
	}

	private void updateRecord(Map<String, String> headers, ImageData imageData) {
		String recordType = headers.get("type");
		String recordId = headers.get("id");
		ClientDataRecordGroup binaryRecordGroup = getBinaryRecordGroup(recordType, recordId);

		updateMasterGroupFromResourceInfo(binaryRecordGroup, imageData);

		dataClient.update(recordType, recordId, binaryRecordGroup);
	}

	private ClientDataRecordGroup getBinaryRecordGroup(String recordType, String recordId) {
		ClientDataRecord binaryRecord = dataClient.read(recordType, recordId);
		return binaryRecord.getDataRecordGroup();
	}

	private void updateMasterGroupFromResourceInfo(ClientDataRecordGroup binaryRecordGroup,
			ImageData imageData) {
		ClientDataGroup resourceInfoGroup = binaryRecordGroup
				.getFirstGroupWithNameInData("resourceInfo");
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

	public ImageAnalyzerFactory onlyForTestGetImageAnalyzerFactory() {
		return imageAnalyzerFactory;

	}

	public String onlyForTestGetOcflHomePath() {
		return ocflHomePath;
	}

	public DataClient onlyForTestGetDataClient() {
		return dataClient;
	}

}
