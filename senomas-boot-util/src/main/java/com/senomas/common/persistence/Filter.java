package com.senomas.common.persistence;

import javax.persistence.criteria.CriteriaBuilder;

public abstract class Filter<T> {
	T data;
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public abstract void init(CriteriaBuilder builder, QueryBuilder query);

}
