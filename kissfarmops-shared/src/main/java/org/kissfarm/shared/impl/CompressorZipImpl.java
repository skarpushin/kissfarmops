package org.kissfarm.shared.impl;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.kissfarm.shared.api.Compressor;

import com.google.common.base.Preconditions;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class CompressorZipImpl implements Compressor {
	@Override
	public String getExtension() {
		return "zip";
	}

	@Override
	public void compress(Set<File> packageContents, File compressedFile) {
		try {
			Preconditions.checkArgument(packageContents != null && packageContents.size() > 0,
					"Arhcive contents must be provided");
			Preconditions.checkArgument(compressedFile != null, "Target archived file name must be provided");

			ZipFile zipFile = new ZipFile(compressedFile);
			ZipParameters parameters = buildZipParameters();

			packageContents.forEach(x -> {
				try {
					if (x.isDirectory()) {
						zipFile.addFolder(x, parameters);
					} else {
						zipFile.addFile(x, parameters);
					}
				} catch (Throwable t2) {
					throw new RuntimeException("Failed to add entry to zip file: " + x, t2);
				}
			});
		} catch (Throwable t) {
			throw new RuntimeException(
					"Failed to compress " + packageContents.size() + " item(s) to archive: " + compressedFile, t);
		}
	}

	private ZipParameters buildZipParameters() {
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		return parameters;
	}

	@Override
	public void decompress(File compressedFile, File targetDirectory) {
		try {
			Preconditions.checkState(targetDirectory.exists() || targetDirectory.mkdirs(),
					"failed to ensure parent folder: %s", targetDirectory);

			ZipFile zip = new ZipFile(compressedFile);
			zip.extractAll(targetDirectory.getAbsolutePath());
		} catch (Throwable t) {
			throw new RuntimeException("Failed to unzip file " + compressedFile + " to " + targetDirectory, t);
		}
	}
}
