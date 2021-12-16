package com.senomas.boot.menu;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.senomas.boot.security.LoginUser;
import com.senomas.boot.security.TokenAuthentication;

@Component
public class MenuServiceImpl implements MenuService {
	private final static Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

	@Autowired
	ApplicationContext ctx;

	public List<MenuObject> getList() {
		return getList(SecurityContextHolder.getContext().getAuthentication());
	}
	
	public List<MenuObject> getList(LoginUser user) {
		return getList(new TokenAuthentication(user));
	}
	
	protected List<MenuObject> getList(Authentication auth) {
		List<MenuObject> result = new LinkedList<>();
		Map<String, MenuObject> map = new LinkedHashMap<>();

		ExpressionParser parser = new SpelExpressionParser();
		SecurityExpressionRoot sctx = new SecurityExpressionRoot(auth) {
		};

		List<MenuObject> list = new LinkedList<>();
		for (Entry<String, Object> me : ctx.getBeansWithAnnotation(Menu.class).entrySet()) {
			Menu menu = ctx.findAnnotationOnBean(me.getKey(), Menu.class);
			for (MenuItem mi : menu.value()) {
				try {
					if (mi.authorize().length() > 0) {
						Expression exp = parser.parseExpression(mi.authorize());
						if ((Boolean) exp.getValue(sctx)) {
							list.add(new MenuObject(mi.order(), mi.id(), mi.path(), me.getKey()));
						}
					} else {
						list.add(new MenuObject(mi.order(), mi.id(), mi.path(), me.getKey()));
					}
				} catch (RuntimeException e) {
					log.warn(me.getKey() + "  " + e.getMessage(), e);
				}
			}
		}
		Collections.sort(list, new Comparator<MenuObject>() {
			@Override
			public int compare(MenuObject o1, MenuObject o2) {
				if (o1.getOrder() > o2.getOrder()) {
					return 1;
				} else if (o1.getOrder() < o2.getOrder()) {
					return -1;
				}
				return o1.getId().compareTo(o2.getId());
			}
		});
		for (MenuObject m : list) {
			map.put(m.getId(), m);
			String id = m.getId();
			int ix = id.lastIndexOf('/');
			if (ix > 0) {
				m.setTitle(m.getId().substring(ix + 1));
				String pk = m.getId().substring(0, ix);
				MenuObject mp = getMenu(result, map, pk);
				mp.addItem(m);
			} else {
				m.setTitle(m.getId());
				result.add(m);
			}
		}
		return result;
	}

	public MenuObject getMenu(List<MenuObject> root, Map<String, MenuObject> menus, String key) {
		MenuObject m = menus.get(key);
		if (m == null) {
			m = new MenuObject(0, key, null, null);
			int ix = key.lastIndexOf('/');
			if (ix > 0) {
				m.setTitle(key.substring(ix + 1));
				MenuObject mp = getMenu(root, menus, key.substring(0, ix));
				mp.addItem(m);
			} else {
				m.setTitle(m.getId());
				root.add(m);
			}
			menus.put(key, m);
		}
		return m;
	}
}
