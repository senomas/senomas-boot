package com.senomas.common.rs;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ExceptionResponse {

	private final Date timestamp;
	
	private final String type;
	
	private final String message;
	
	private final String stackTrace[];
	
	public ExceptionResponse(Exception exception) {
		this.type = exception.getClass().getName();
		this.timestamp = new Date();
		this.message = exception.getMessage();
		StackTraceElement[] est = exception.getStackTrace();
		stackTrace = new String[est.length];
		for (int i=0, il=est.length; i<il; i++) {
			stackTrace[i] = est[i].toString();
		}
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}
	
	public String[] getStackTrace() {
		return stackTrace;
	}
}
