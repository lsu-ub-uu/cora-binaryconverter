package se.uu.ub.cora.binaryconverter.messagereceiver;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binaryconverter.document.PdfConverterFactory;
import se.uu.ub.cora.binaryconverter.spy.DataClientSpy;
import se.uu.ub.cora.binaryconverter.spy.PdfConverterFactorySpy;
import se.uu.ub.cora.clientdata.ClientDataProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataFactorySpy;

public class ConvertPdfToThumbnailsTest {

	private ClientDataFactorySpy clientDataFactory;
	private ConvertPdfToThumbnails converter;
	private static final String SOME_FILE_STORAGE_BASE_PATH = "/someOutputPath/";
	private static final String ARCHIVE_BASE_PATH = "/someOcflRootHome";
	private DataClientSpy dataClient;
	PdfConverterFactory pdfConverterFactory;

	@BeforeMethod
	public void beforeMethod() {
		// setUpImageAnalyzerFactory();

		dataClient = new DataClientSpy();
		pdfConverterFactory = new PdfConverterFactorySpy();

		// imageConverterFactory = new ImageConverterFactorySpy();

		// imageSmallConverter = new AnalyzeAndConvertToThumbnails(dataClient, ARCHIVE_BASE_PATH,
		// SOME_FILE_STORAGE_BASE_PATH, imageConverterFactory);
		//
		// setMessageHeaders();
		clientDataFactory = new ClientDataFactorySpy();
		ClientDataProvider.onlyForTestSetDataFactory(clientDataFactory);
	}

	@Test
	public void testConvertPDfToThumbnailCalled() throws Exception {

		converter = new ConvertPdfToThumbnails(dataClient, ARCHIVE_BASE_PATH,
				SOME_FILE_STORAGE_BASE_PATH, pdfConverterFactory);

	}

}
