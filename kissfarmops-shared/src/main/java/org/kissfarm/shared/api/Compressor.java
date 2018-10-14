package org.kissfarm.shared.api;

import java.io.File;
import java.util.Set;

/**
 * Generic compressor interface. Underlying implementation coul use zip, tar, or
 * something else
 * 
 * @author sergeyk
 *
 */
public interface Compressor {
	String getExtension();

	/**
	 * Create archive
	 * 
	 * @param packageContents file and/or folders
	 * @param compressedFile  target file
	 */
	void compress(Set<File> packageContents, File compressedFile);

	void decompress(File compressedFile, File targetDirectory);
}
