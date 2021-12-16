package com.senomas.common.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Forbidden")
public class ForbiddenException extends RuntimeException {
	private static final long serialVersionUID = 1676087038885165338L;

	public ForbiddenException(String msg) {
		super(msg);
	}
}
