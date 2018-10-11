package org.kissfarm.controller.config.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kissfarm.controller.config.api.FarmConfig;
import org.kissfarm.controller.config.api.FarmConfigFolderReader;
import org.kissfarm.controller.config.api.TagsToAppsMapping;
import org.kissfarm.shared.api.AppDefFolderReader;
import org.kissfarm.shared.config.dto.AppDefConfig;
import org.kissfarm.shared.impl.AppDefFolderReaderImpl;
import org.kissfarm.shared.tools.Defaults;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

public class FarmConfigFolderReaderImpl implements FarmConfigFolderReader {
	private static final String FILE_TAGS_TO_APPS_MAPPING = "TagsToAppsMapping.json";

	private Gson gson = new Gson();
	private AppDefFolderReader appDefFolderReader = new AppDefFolderReaderImpl();

	@Override
	public FarmConfig readFarmConfig(File dir, String version) {
		try {
			FarmConfig ret = new FarmConfig();
			ret.setVersion(version);
			ret.setTagsToAppsMapping(readTagsToAppsMapping(dir));
			ret.setAppDefs(readAppDefinitions(dir));

			Preconditions.checkState(ret.getAppDefs().size() > 0, "Config must contain at least 1 app definition");
			ret.getTagsToAppsMapping().values().forEach(x -> Preconditions.checkState(ret.getAppDefs().containsKey(x),
					"Application %s is mentioned in tags-to-apps mapping, but missing in config", x));

			addImplicitTagsMapping(ret);

			return ret;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to read Farm Config from clonned repo", t);
		}
	}

	protected Map<String, AppDefConfig> readAppDefinitions(File stagingDir) {
		WildcardFileFilter dirFilter = new WildcardFileFilter(AppDefFolderReaderImpl.PREFIX_APP + "*");
		Iterator<File> iterator = FileUtils.iterateFilesAndDirs(stagingDir, DirectoryFileFilter.DIRECTORY, dirFilter);
		List<AppDefConfig> ret = new ArrayList<>();
		for (; iterator.hasNext();) {
			File appDir = iterator.next();
			if (!dirFilter.accept(appDir)) {
				continue;
			}

			AppDefConfig appDef = appDefFolderReader.readAppDefConfig(appDir);
			ret.add(appDef);
		}
		return ret.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
	}

	protected void addImplicitTagsMapping(FarmConfig ret) {
		ret.getAppDefs().keySet().forEach(x -> ret.getTagsToAppsMapping().put(x, x));
	}

	protected TagsToAppsMapping readTagsToAppsMapping(File stagingDir) throws IOException {
		File tagsMapping = new File(stagingDir + File.separator + FILE_TAGS_TO_APPS_MAPPING);
		if (tagsMapping.exists()) {
			String readFileToStringStr = FileUtils.readFileToString(tagsMapping, Defaults.ENCODING);
			TagsToAppsMapping fromJson = gson.fromJson(readFileToStringStr, TagsToAppsMapping.class);
			return fromJson;
		} else {
			return new TagsToAppsMapping();
		}
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public AppDefFolderReader getAppDefinitionFolderReader() {
		return appDefFolderReader;
	}

	public void setAppDefinitionFolderReader(AppDefFolderReader appDefFolderReader) {
		this.appDefFolderReader = appDefFolderReader;
	}

}
