package se.uu.ub.cora.binaryconverter.common;

public interface PathBuilder {

	/**
	 * buildPathToAResourceInArchive method builds a path to a resource stored in archive using
	 * type, id and datadivider.
	 * <p>
	 * It can throws ImageConverterException if an error ocurr.
	 * @param dataDivider
	 * @param type
	 * @param id
	 * 
	 * @return
	 */
	String buildPathToAResourceInArchive(String dataDivider, String type, String id);

	/**
	 * buildPathToAFileAndEnsureFolderExists method builds a path to a file to be stored in
	 * fileSystem. It also ensures that the folder containing the file exists, if not it creates the
	 * necessary folders.
	 * <p>
	 * It can throws ImageConverterException if an error ocurr.
	 * @param dataDivider
	 *            datadivider of a the record
	 * @param type
	 *            is a String with the type of the record.
	 * @param id
	 *            is a String with the id of the file.
	 * 
	 * @return
	 */
	String buildPathToAFileAndEnsureFolderExists(String dataDivider, String type,
			String id);

}
