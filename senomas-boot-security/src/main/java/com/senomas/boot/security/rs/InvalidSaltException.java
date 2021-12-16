package com.senomas.boot.security.rs;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Forbidden")
public class InvalidSaltException extends AuthenticationException {
	private static final long serialVersionUID = 1676087038885165338L;
	
	private final long saltTimestamp;
	
	private final String salt;

	public InvalidSaltException(long saltTimestamp, String salt, String msg) {
		super(msg);
		this.saltTimestamp = saltTimestamp;
		this.salt = salt;
	}
	
	public long getSaltTimestamp() {
		return saltTimestamp;
	}
	
	public String getSalt() {
		return salt;
	}

}
