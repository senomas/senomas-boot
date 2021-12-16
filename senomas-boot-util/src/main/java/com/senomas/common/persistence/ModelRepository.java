package com.senomas.common.persistence;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ModelRepository<T, K extends Serializable> extends JpaRepository<T, K>, JpaSpecificationExecutor<T> {

}
