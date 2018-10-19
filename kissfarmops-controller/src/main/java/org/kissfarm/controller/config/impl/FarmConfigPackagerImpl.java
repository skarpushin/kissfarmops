package org.kissfarm.controller.config.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.FarmConfigPackager;
import org.kissfarm.controller.config.dto.FarmConfig;
import org.kissfarm.controller.services.nodes.api.TagParser;
import org.kissfarm.controller.services.nodes.impl.TagParserImpl;
import org.kissfarm.shared.api.Compressor;
import org.kissfarm.shared.config.dto.AppDefConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

public class FarmConfigPackagerImpl implements FarmConfigPackager {
	private Logger log = LoggerFactory.getLogger(getClass());

	private Compressor compressor;
	private TagParser tagParser = new TagParserImpl();
	private FarmConfigFolderReader farmConfigFolderReader = new FarmConfigFolderReaderImpl();
	private File packagesFolder;

	public FarmConfigPackagerImpl(String packagesFolder) {
		Preconditions.checkArgument(packagesFolder != null, "Package folder must be provided");
		this.packagesFolder = new File(packagesFolder);
		Preconditions.checkArgument(GitAbstractionImpl.isCanWriteToFolder(this.packagesFolder),
				"Folder %s must be writable", packagesFolder);
	}

	@Override
	public String preparePackage(FarmConfig farmConfig, String workTreeFolder, String nodeTags) {
		try {
			Set<String> tags = verifyAndParseTags(nodeTags);

			return internalCreatePackage(farmConfig, workTreeFolder, tags);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to preparePackage for " + nodeTags, t);
		}
	}

	private String internalCreatePackage(FarmConfig farmConfig, String workTreeFolder, Set<String> tags) {
		Preconditions.checkArgument(farmConfig != null, "Farm Config required");
		File workTreeFolderFile = verifyAndGetWorkTreeFolder(workTreeFolder);

		File packageFile = buildPackageFilename(farmConfig, tags);
		if (packageFile.exists()) {
			log.debug("No need to create package again, it was already created");
			return packageFile.getAbsolutePath();
		}
		Preconditions.checkState(packageFile.getParentFile().exists() || packageFile.getParentFile().mkdirs(),
				"Failed to create parent dirs for package file: %s", packageFile.getAbsolutePath());

		String tagsStr = Arrays.toString(tags.toArray());
		log.debug("Creating package for tags: {}", tagsStr);
		Set<AppDefConfig> appDefs = FarmConfigTools.findAppDefConfigByTags(tags, farmConfig);
		Preconditions.checkArgument(!appDefs.contains(null), "Failed to resolve tags-to-app mapping: " + tagsStr);

		Set<File> packageContents = appDefs.stream()
				.map(x -> farmConfigFolderReader.buildAppDefDirByAppName(workTreeFolderFile, x.getName()))
				.collect(Collectors.toSet());
		compressor.compress(packageContents, packageFile);

		return packageFile.getAbsolutePath();
	}

	@Override
	public String preparePackage(FarmConfig farmConfig, String workTreeFolder, Set<String> nodeTags) {
		try {
			return internalCreatePackage(farmConfig, workTreeFolder, nodeTags);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to preparePackage for (array) " + Arrays.toString(nodeTags.toArray()),
					t);
		}
	}

	private File buildPackageFilename(FarmConfig farmConfig, Set<String> tags) {
		String tagsStrSorted = tags.stream().sorted().collect(Collectors.joining(","));
		String checksum = getStringChecksum(farmConfig.getVersion() + tagsStrSorted);
		File packageFile = new File(getPackageFullPathByBaseName(checksum));
		return packageFile;
	}

	@Override
	public String getPackageFullPathByBaseName(String basePackageName) {
		return packagesFolder.getAbsolutePath() + File.separator + basePackageName + "." + compressor.getExtension();
	}

	private Set<String> verifyAndParseTags(String nodeTags) {
		Preconditions.checkArgument(nodeTags != null);
		Set<String> tags = new HashSet<>(tagParser.parseTags(nodeTags));
		Preconditions.checkArgument(tags.size() > 0, "at least 1 tag must be provided");
		return tags;
	}

	private File verifyAndGetWorkTreeFolder(String workTreeFolder) {
		Preconditions.checkArgument(workTreeFolder != null, "workTreeFolder must be provided");
		File workTreeFolderFile = new File(workTreeFolder);
		Preconditions.checkArgument(workTreeFolderFile.exists() && workTreeFolderFile.isDirectory(),
				"workTreeFolder must point to a valid folder");
		return workTreeFolderFile;
	}

	private String getStringChecksum(String tagsStrSorted) {
		CRC32 crc = new CRC32();
		crc.update(tagsStrSorted.getBytes(Charset.forName("UTF-8")));
		String checksum = tagsStrSorted.length() + "_" + crc.getValue();
		return checksum;
	}

	public TagParser getTagParser() {
		return tagParser;
	}

	public void setTagParser(TagParser tagParser) {
		this.tagParser = tagParser;
	}

	public Compressor getCompressor() {
		return compressor;
	}

	@Autowired
	public void setCompressor(Compressor compressor) {
		this.compressor = compressor;
	}

}
