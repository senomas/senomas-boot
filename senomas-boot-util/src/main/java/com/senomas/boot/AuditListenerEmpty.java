package com.senomas.boot;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AuditListenerEmpty implements AuditListener {

	@Override
	public void onCreate(String type, JsonNode object) {
	}
	
	@Override
	public void onUpdate(String type, JsonNode diff, JsonNode object, JsonNode old) {
	}

	@Override
	public void onDelete(String type, JsonNode object) {
	}
}
