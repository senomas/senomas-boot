package com.senomas.boot.menu;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.senomas.common.rs.Views;

public class MenuObject {
	@JsonView(Views.List.class)
	private int order;

	@JsonView(Views.List.class)
	private String id;

	@JsonView(Views.List.class)
	private String title;

	@JsonView(Views.List.class)
	private String path;

	@JsonView(Views.List.class)
	private String component;

	@JsonView(Views.List.class)
	private List<MenuObject> items;

	public MenuObject(int order, String id, String path, String component) {
		super();
		this.order = order;
		this.id = id;
		this.path = path;
		this.component = component;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<MenuObject> getItems() {
		return items;
	}

	public void setItems(List<MenuObject> items) {
		this.items = items;
	}

	public void addItem(MenuObject item) {
		if (items == null)
			items = new LinkedList<>();
		items.add(item);
	}
}
