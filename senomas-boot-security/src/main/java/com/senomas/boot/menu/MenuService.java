package com.senomas.boot.menu;

import java.util.List;

import com.senomas.boot.security.LoginUser;

public interface MenuService {
	
	List<MenuObject> getList();

	List<MenuObject> getList(LoginUser user);

}
