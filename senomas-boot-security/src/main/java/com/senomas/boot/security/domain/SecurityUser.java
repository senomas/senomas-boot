package com.senomas.boot.security.domain;

import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
public interface SecurityUser {

	String getLogin();

	String getLoginToken();
	
	void setLoginToken(String loginToken);
	
	Date getLastLogin();
	
	List<GrantedAuthority> getAuthorities();

}
