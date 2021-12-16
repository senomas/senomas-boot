package com.senomas.common.persistence;

import com.senomas.common.rs.PageParam;

public class RepositoryPageParam<T extends Filter<FT>, FT> extends PageParam<FT> {

	@SuppressWarnings("unchecked")
	public T getFilter() {
		return (T) filter;
	}

	public void setFilter(T filter) {
		this.filter = filter;
	}
}
