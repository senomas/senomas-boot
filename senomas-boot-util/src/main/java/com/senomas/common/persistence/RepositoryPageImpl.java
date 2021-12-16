package com.senomas.common.persistence;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.senomas.common.rs.PageParam;

public class RepositoryPageImpl<T> extends AbstractCustomRepository implements RepositoryPage<T> {

	@Autowired
	EntityManager em;

	@Override
	public Page<T> findFilter(PageParam param, Class<T> type) {
		return findWithSpecification(param.getRequestId(), em, param.getRequest(), param.getFilter(), type);
	}

}
