package com.senomas.common.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Unauthorized")
public class UnauthorizedException extends RuntimeException {
	private static final long serialVersionUID = 1676087038885165338L;

	public UnauthorizedException(String msg) {
		super(msg);
	}
}
