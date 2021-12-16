package com.senomas.common;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class DataInitializer {
	
	protected final static Random rnd = new Random();

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager txManager;

	@PostConstruct
	public final void _populate() {
		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					populate();
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		});
	}
	
	protected abstract void populate() throws Exception;

}
