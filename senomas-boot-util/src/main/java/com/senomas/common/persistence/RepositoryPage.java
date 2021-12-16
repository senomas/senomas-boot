package com.senomas.common.persistence;

import org.springframework.data.domain.Page;

import com.senomas.common.rs.PageParam;

public interface RepositoryPage<T> {
	
	Page<T> findFilter(PageParam param, Class<T> type);

}
