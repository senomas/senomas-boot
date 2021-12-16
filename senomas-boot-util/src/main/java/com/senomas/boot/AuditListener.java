package com.senomas.boot;

import com.fasterxml.jackson.databind.JsonNode;

public interface AuditListener {

	void onCreate(String type, JsonNode object);
	
	void onUpdate(String type, JsonNode diff, JsonNode object, JsonNode old);
	
	void onDelete(String type, JsonNode object);
}
