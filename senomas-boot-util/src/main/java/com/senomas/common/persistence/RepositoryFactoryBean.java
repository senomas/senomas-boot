package com.senomas.common.persistence;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

public class RepositoryFactoryBean<T extends JpaRepository<S, ID>, S, ID extends Serializable> extends JpaRepositoryFactoryBean<T, S, ID> implements ApplicationContextAware {
	ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	protected RepositoryFactory createRepositoryFactory(EntityManager entityManager) {
        return new RepositoryFactory(entityManager, context);
    }
}
