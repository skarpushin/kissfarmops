package org.kissfarm.shared.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kissfarm.shared.api.AppDefFolderReader;
import org.kissfarm.shared.config.dto.ActionCommands;
import org.kissfarm.shared.config.dto.ActionConfig;
import org.kissfarm.shared.config.dto.AppDefConfig;
import org.kissfarm.shared.config.dto.AppProtoConfig;
import org.kissfarm.shared.tools.Defaults;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

public class AppDefFolderReaderImpl implements AppDefFolderReader {
	public static final String PREFIX_APP = "app-";
	public static final String FILE_APP_DEFINITION = "app-definition.json";
	public static final String PREFIX_ACTION = "action-";
	public static final List<String> ALLOWED_STATUS_TYPES = Arrays.asList("String", "Integer", "Long", "Boolean");

	private Gson gson = new Gson();

	@Override
	public AppDefConfig readAppDefConfig(File appDir) {
		try {
			String name = getAppNameFromDirName(appDir);
			AppDefConfig appDef = readAppDefinitionConfigFile(appDir, name);
			readAdditionalActions(appDir, appDef);
			assertStatusFieldsTypes(appDef.getStatusSchema());
			addDefaultProtoIfNeeded(appDef);
			return appDef;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to read app definition from: " + appDir, t);
		}
	}

	protected void addDefaultProtoIfNeeded(AppDefConfig appDef) {
		if (appDef.getPrototypes() != null) {
			return;
		}
		AppProtoConfig defaultPrototype = new AppProtoConfig();
		defaultPrototype.setName("default");
		Map<String, AppProtoConfig> prototypes = new HashMap<>();
		prototypes.put(defaultPrototype.getName(), defaultPrototype);
		appDef.setPrototypes(prototypes);
	}

	protected void assertStatusFieldsTypes(Map<String, String> statusSchema) {
		if (statusSchema == null) {
			return;
		}

		statusSchema.entrySet()
				.forEach(x -> Preconditions.checkArgument(ALLOWED_STATUS_TYPES.contains(x.getValue()),
						"Status field %s is not of one of the following types: %s", x.getKey(),
						Arrays.toString(ALLOWED_STATUS_TYPES.toArray())));
	}

	protected String getAppNameFromDirName(File appDir) {
		String name = FilenameUtils.getBaseName(appDir.getAbsolutePath());
		name = name.substring(PREFIX_APP.length());
		return name;
	}

	protected void readAdditionalActions(File appDir, AppDefConfig appDef) {
		Map<String, ActionConfig> actionsFromDirs = readActionsFromDirs(appDir);
		if (appDef.getActions() == null) {
			appDef.setActions(new HashMap<>());
		}
		appDef.getActions().putAll(actionsFromDirs);
		appDef.getActions().values().forEach(x -> {
			if (x.getCommands() == null) {
				x.setCommands(new ActionCommands());
			}
			ActionCommands cmds = x.getCommands();
			if (cmds.getInvoke() == null) {
				cmds.setInvoke(buildDefaultInvokeCommand());
				File invokeFile = new File(appDir + File.separator + PREFIX_ACTION + x.getName());
				Preconditions.checkArgument(invokeFile.exists(),
						"Was trying to build default action %s command, but %s was not found", x.getName(), invokeFile);
			}
		});

		// NOTE: Action might have commands but don't have scripts - that might be the
		// case
	}

	protected String[] buildDefaultInvokeCommand() {
		return new String[] { "sh", "-c", "invoke.sh" };
	}

	protected Map<String, ActionConfig> readActionsFromDirs(File appDir) {
		WildcardFileFilter dirFilter = new WildcardFileFilter(PREFIX_ACTION + "*");
		Iterator<File> iterator = FileUtils.iterateFilesAndDirs(appDir, DirectoryFileFilter.DIRECTORY, dirFilter);
		Map<String, ActionConfig> ret = new HashMap<>();
		for (; iterator.hasNext();) {
			File actionDir = iterator.next();
			if (!dirFilter.accept(actionDir)) {
				continue;
			}

			String name = FilenameUtils.getBaseName(actionDir.getAbsolutePath());
			name = name.substring(PREFIX_ACTION.length());
			ActionConfig action = new ActionConfig();
			action.setName(name);
			ret.put(name, action);
		}

		return ret;
	}

	protected AppDefConfig readAppDefinitionConfigFile(File appDir, String name) throws IOException {
		File appDefFile = new File(appDir.getAbsolutePath() + File.separator + FILE_APP_DEFINITION);
		AppDefConfig appDef = new AppDefConfig();
		if (appDefFile.exists()) {
			appDef = gson.fromJson(FileUtils.readFileToString(appDefFile, Defaults.ENCODING), AppDefConfig.class);
		} else {
			appDef = new AppDefConfig();
		}

		// set defaults
		if (!StringUtils.hasText(appDef.getName())) {
			appDef.setName(name);
		}
		if (!StringUtils.hasText(appDef.getDisplayName())) {
			appDef.setDisplayName(name);
		}
		return appDef;
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

}
