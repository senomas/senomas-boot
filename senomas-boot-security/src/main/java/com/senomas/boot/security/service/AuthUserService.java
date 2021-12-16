package com.senomas.boot.security.service;

import com.senomas.boot.security.domain.SecurityUser;

public interface AuthUserService {

	SecurityUser findByLogin(String login);
	
	SecurityUser save(SecurityUser user);

}
