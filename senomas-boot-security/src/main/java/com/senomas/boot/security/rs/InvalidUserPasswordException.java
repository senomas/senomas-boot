package com.senomas.boot.security.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Forbidden")
public class InvalidUserPasswordException extends AuthenticationException {
	private static final long serialVersionUID = 1676087038885165338L;
	
	public InvalidUserPasswordException() {
		super("Invalid user password");
	}
	
	@Override
	public StackTraceElement[] getStackTrace() {
		return null;
	}
}
