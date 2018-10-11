package org.kissfarm.shared.impl;

import java.io.File;

import org.kissfarm.shared.api.ZipUtils;

import com.google.common.base.Preconditions;

import net.lingala.zip4j.core.ZipFile;

public class ZipUtilsImpl implements ZipUtils {
	@Override
	public void unzip(File zipFile, File intoDestinationFolder) {
		try {
			Preconditions.checkArgument(intoDestinationFolder.exists() || intoDestinationFolder.mkdir(),
					"Failed to ensure target folder %s" + intoDestinationFolder);

			ZipFile zip = new ZipFile(zipFile);
			zip.extractAll(intoDestinationFolder.getAbsolutePath());
		} catch (Throwable t) {
			throw new RuntimeException("Failed to unzip file " + zipFile + " to " + intoDestinationFolder, t);
		}
	}
}
