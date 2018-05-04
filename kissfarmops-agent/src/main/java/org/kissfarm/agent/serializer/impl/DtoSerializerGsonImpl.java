package org.kissfarm.agent.serializer.impl;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.kissfarm.agent.serializer.api.DtoSerializer;

import com.google.gson.Gson;

public class DtoSerializerGsonImpl implements DtoSerializer {
	private static final Charset ENCODING = Charset.forName("UTF-8");

	private Gson gson = new Gson();

	@Override
	public void save(Object dto, File file) {
		try {
			String json = gson.toJson(dto);
			FileUtils.write(file, json, ENCODING);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to save " + dto, t);
		}
	}

	@Override
	public <T extends Serializable> T load(File file, Class<T> clazz) {
		try {
			String str = FileUtils.readFileToString(file, ENCODING);
			return gson.fromJson(str, clazz);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load " + clazz + " from " + file, t);
		}
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

}
