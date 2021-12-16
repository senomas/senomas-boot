package com.senomas.boot.security.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.senomas.boot.menu.MenuObject;
import com.senomas.boot.menu.MenuService;
import com.senomas.boot.security.LoginRequest;
import com.senomas.boot.security.LoginUser;
import com.senomas.boot.security.domain.AuthToken;
import com.senomas.boot.security.domain.SecurityUser;
import com.senomas.boot.security.rs.AuthenticationException;
import com.senomas.boot.security.service.AuthenticationService;
import com.senomas.boot.security.service.TokenService;
import com.senomas.common.rs.Views;

@RestController
public class AuthenticationRestService {
//	private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
	
	@Autowired
	AuthenticationService service;
	
	@Autowired
	MenuService menuService;
	
	@Autowired
	TokenService tokenService;
	
	@RequestMapping(value = "${rest.auth.uri:${rest.uri}/auth}/login", method = { RequestMethod.POST })
	public AuthTokenMenu login(HttpServletRequest request, @RequestBody(required=false) LoginRequest login) {
		AuthToken token = service.login(request, login);
		return new AuthTokenMenu(token, menuService.getList(new LoginUser(token.getToken(), token.getUser())));
	}
	
	@RequestMapping(value = "${rest.auth.uri:${rest.uri}/auth}/refresh/{login}/{refreshToken}", method = { RequestMethod.GET })
	public AuthToken refresh(HttpServletRequest request, @PathVariable("login") String login, @PathVariable("refreshToken") String refreshToken) {
		return service.refresh(request, login, refreshToken);
	}
	
	@RequestMapping(value = "/${rest.uri}/login-user", method = { RequestMethod.GET })
	public UserMenu getUser() {
		SecurityUser user = service.getUser();
		if (user == null) throw new AuthenticationException("No user");
		return new UserMenu(user, menuService.getList());
	}
	
	@RequestMapping(value = "/${rest.uri}/logout", method = { RequestMethod.GET })
	public SecurityUser logout() {
		return service.logout();
	}
	
	@JsonView(Views.List.class)
	@RequestMapping(value = "/${rest.uri}/menu", method = RequestMethod.GET)
	public List<MenuObject> getList() {
		return menuService.getList();
	}
	
	@Profile("dev")
	@RequestMapping(value = "${rest.auth.uri:${rest.uri}/auth}/logout/{login}", method = { RequestMethod.GET })
	public SecurityUser devLogout(@PathVariable("login") String login) {
		return service.logout(login);
	}
	
	@Profile("dev")
	@RequestMapping(value = "${rest.auth.uri:${rest.uri}/auth}/token", method = { RequestMethod.GET })
	public List<LoginUser> getTokens() {
		return tokenService.getList();
	}
	
	@Profile("dev")
	@RequestMapping(value = "${rest.auth.uri:${rest.uri}/auth}/token/clear", method = { RequestMethod.POST })
	public void clearToken() {
		tokenService.clear();
	}

	@Profile("dev")
	@RequestMapping(value = "${rest.auth.uri:${rest.uri}/auth}/token/{token}", method = { RequestMethod.GET })
	public LoginUser getTokens(@PathVariable("token") String token) {
		return tokenService.get(token);
	}
	
	public static class UserMenu {
		final SecurityUser user;
		final List<MenuObject> menu;
		
		public UserMenu(SecurityUser user, List<MenuObject> menu) {
			super();
			this.user = user;
			this.menu = menu;
		}

		public SecurityUser getUser() {
			return user;
		}

		public List<MenuObject> getMenu() {
			return menu;
		}
	}
	
	
	public static class AuthTokenMenu {
		final AuthToken token;
		final List<MenuObject> menu;

		public AuthTokenMenu(AuthToken token, List<MenuObject> menu) {
			super();
			this.token = token;
			this.menu = menu;
		}

		public AuthToken getToken() {
			return token;
		}

		public List<MenuObject> getMenu() {
			return menu;
		}
	}


}
