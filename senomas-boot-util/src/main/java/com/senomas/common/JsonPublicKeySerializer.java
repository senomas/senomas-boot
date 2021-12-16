package com.senomas.common;

import java.io.IOException;
import java.security.PublicKey;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonPublicKeySerializer extends JsonSerializer<PublicKey> {

	@Override
	public void serialize(PublicKey value, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		gen.writeBinary(value.getEncoded());
	}

}
