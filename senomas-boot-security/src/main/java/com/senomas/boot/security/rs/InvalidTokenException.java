package com.senomas.boot.security.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Invalid token")
public class InvalidTokenException extends AuthenticationException {
	private static final long serialVersionUID = 1676087038885165338L;

	public InvalidTokenException(String msg) {
		super(msg);
	}
}
