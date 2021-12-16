package com.senomas.common.persistence;

import java.util.Date;

import org.springframework.data.domain.Page;

public interface PageRequestId<T> extends Page<T>{

	String getRequestId();
	
	Date getTimestamp();
	
}
