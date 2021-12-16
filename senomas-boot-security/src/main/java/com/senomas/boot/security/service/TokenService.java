package com.senomas.boot.security.service;

import java.util.List;

import com.senomas.boot.security.LoginUser;
import com.senomas.boot.security.domain.SecurityUser;

public interface TokenService {

	LoginUser create(SecurityUser user);
	
	LoginUser get(String token);
	
	LoginUser getByLogin(String login);
	
	List<LoginUser> getList();
	
	void remove(String token);

	void clear();
}
