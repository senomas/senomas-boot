package com.senomas.common.logback.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Document(collection="#{logDumpCollectionName}")
@JsonInclude(Include.NON_NULL)
public class LogDump {
	@Id
    private String id;
	
	@Field("ts")
	private Date timestamp;
	
	@Field("msg")
	private String message;
	
	private String level;

	private String logger;
	
	private String thread;
	
	private List<StackTraceDump> caller;
	
	private List<StackTraceDump> stacks;
	
	private Map<String, String> mdc;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}
	
	public List<StackTraceDump> getCaller() {
		return caller;
	}
	
	public void setCaller(List<StackTraceDump> caller) {
		this.caller = caller;
	}
	
	public List<StackTraceDump> getStacks() {
		return stacks;
	}
	
	public void setStacks(List<StackTraceDump> stacks) {
		this.stacks = stacks;
	}

	public Map<String, String> getMdc() {
		return mdc;
	}

	public void setMdc(Map<String, String> mdc) {
		this.mdc = mdc;
	}
}
