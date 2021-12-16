package com.senomas.common.logback;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ConfigHack {
	
	@Value("${com.senomas.common.logback.nosql.collection}")
	private String logDumpCollectionName;

	
	@Bean
	public String logDumpCollectionName() {
		return logDumpCollectionName;
	}

}
