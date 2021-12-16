package com.senomas.common.rs;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

public class Json {
	private final String raw;
	
	public Json(String raw) {
		this.raw = raw;
	}

	@JsonValue
	@JsonRawValue
	public String getRaw() {
		return raw;
	}
	
}
