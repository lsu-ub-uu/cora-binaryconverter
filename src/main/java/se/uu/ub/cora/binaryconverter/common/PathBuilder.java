package se.uu.ub.cora.binaryconverter.common;

public interface PathBuilder {

	/**
	 * buildPathToAResourceInArchive method builds a path to a resource stored in archive using
	 * type, id and datadivider.
	 * <p>
	 * It can throws ImageConverterException if an error ocurr.
	 * 
	 * @param type
	 * @param id
	 * @param dataDivider
	 * @return
	 */
	String buildPathToAResourceInArchive(String type, String id, String dataDivider);

}
