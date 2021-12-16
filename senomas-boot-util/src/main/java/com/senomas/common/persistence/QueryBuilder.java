package com.senomas.common.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class QueryBuilder {
	final CriteriaBuilder builder;
	Predicate predicate;
	List<Order> orderBy;
	Root<?> root;
	Map<String, Root<?>> rootMap;
	
	public QueryBuilder(CriteriaBuilder builder, Root<?> root) {
		this.builder = builder;
		this.root = root;
	}
	
	public QueryBuilder(CriteriaBuilder builder, Root<?> root, Map<String, Root<?>> rootMap) {
		this.builder = builder;
		this.root = root;
		this.rootMap = rootMap;
	}
	
	public Root<?> root() {
		return root;
	}
	
	public Root<?> root(String name) {
		if (!rootMap.containsKey(name)) throw new RuntimeException("Root '"+name+"' not found in "+root.getJavaType().getName());
		return rootMap.get(name);
	}
	
	public void whereAddAnd(Predicate p) {
		if (predicate != null) {
			predicate = builder.and(predicate, p);
		} else {
			predicate = p;
		}
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void addOrderBy(Order order) {
		if (orderBy == null) orderBy = new LinkedList<>();
		orderBy.add(order);
	}
	
	public List<Order> getOrderBy() {
		return orderBy;
	}

}
