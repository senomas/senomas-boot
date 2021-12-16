package com.senomas.common.persistence;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.senomas.common.rs.PageParam;

@NoRepositoryBean
public interface PageRepository<T, ID extends Serializable, F extends Filter<FT>, FT> extends JpaRepository<T, ID> {
	
	boolean exists(T object);
	
	Page<T> findFilter(PageParam<FT> param, Class<T> paramType);

}
