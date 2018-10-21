package org.kissfarm.shared.config.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import com.google.gson.Gson;

public class StatusSchemaFromStringConverter implements Converter<String, StatusSchema> {
	private Gson gson = new Gson();

	@Override
	public StatusSchema convert(String source) {
		return gson.fromJson(source, StatusSchema.class);
	}

	public Gson getGson() {
		return gson;
	}

	@Autowired(required = false)
	public void setGson(Gson gson) {
		this.gson = gson;
	}
}
