package com.senomas.common.persistence;

import javax.persistence.criteria.CriteriaBuilder;

public class NoFilter<T> extends Filter<T> {

	@Override
	public void init(CriteriaBuilder builder, QueryBuilder query) {
	}

}
