package com.senomas.common;

import java.io.IOException;
import java.security.PublicKey;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class JsonPublicKeyDeserializer extends JsonDeserializer<PublicKey> {

	@Override
	public PublicKey deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		return U.getPublicKey(parser.getBinaryValue());
	}

}
