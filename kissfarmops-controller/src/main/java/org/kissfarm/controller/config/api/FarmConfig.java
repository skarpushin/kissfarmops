package org.kissfarm.controller.config.api;

import java.util.Map;

import org.kissfarm.shared.config.dto.AppDefConfig;
import org.summerb.approaches.jdbccrud.common.DtoBase;

public class FarmConfig implements DtoBase {
	private static final long serialVersionUID = 650527880882262397L;

	private String version;

	private Map<String, AppDefConfig> appDefs;

	/**
	 * NOTE: This mapping can extends implicit mapping. If node has TagA and there
	 * is an application with name TagA it will be considered implicit mapping
	 */
	private TagsToAppsMapping tagsToAppsMapping;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, AppDefConfig> getAppDefs() {
		return appDefs;
	}

	public void setAppDefs(Map<String, AppDefConfig> appDefs) {
		this.appDefs = appDefs;
	}

	public TagsToAppsMapping getTagsToAppsMapping() {
		return tagsToAppsMapping;
	}

	public void setTagsToAppsMapping(TagsToAppsMapping tagsToAppsMapping) {
		this.tagsToAppsMapping = tagsToAppsMapping;
	}
}
