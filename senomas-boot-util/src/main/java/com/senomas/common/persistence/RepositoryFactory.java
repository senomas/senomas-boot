package com.senomas.common.persistence;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.util.Assert;

public class RepositoryFactory extends JpaRepositoryFactory {

	@SuppressWarnings("unused")
	private final EntityManager entityManager;
	
	private final ApplicationContext context;
	
	public RepositoryFactory(EntityManager entityManager, ApplicationContext context) {
		super(entityManager);
		Assert.notNull(entityManager);
		Assert.notNull(context);
		this.entityManager = entityManager;
		this.context = context;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected <T, ID extends Serializable> SimpleJpaRepository<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
		JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
		
		Class<?> repositoryInterface = information.getRepositoryInterface();

		SimpleJpaRepository<?, ?> repository;

		if (isQueryDslExecutor(repositoryInterface)) {
			repository = new QueryDslJpaRepository(entityInformation, entityManager);
		} else {
			repository = new PageRepositoryImpl(entityInformation, entityManager, repositoryInterface);
		}
		
		context.getAutowireCapableBeanFactory().autowireBean(repository);
		
		return repository;
	}
	
	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslExecutor(metadata.getRepositoryInterface())) {
			return QueryDslJpaRepository.class;
		} else {
			return PageRepositoryImpl.class;
		}
	}

	private boolean isQueryDslExecutor(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}
}
