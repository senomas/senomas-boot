package com.senomas.boot.security.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Invalid refreshToken")
public class InvalidRefreshTokenException extends AuthenticationException {
	private static final long serialVersionUID = 1676087038885165338L;

	public InvalidRefreshTokenException(String msg) {
		super(msg);
	}
}
