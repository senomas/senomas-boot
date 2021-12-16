package com.senomas.common.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.Column;
import javax.persistence.criteria.CriteriaBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicFilter<T> extends Filter<T> {
	private final static Logger log = LoggerFactory.getLogger(BasicFilter.class);

	@Override
	public void init(CriteriaBuilder builder, QueryBuilder query) {
		if (data != null) {
			Class<?> dc = data.getClass();
			for (Field f : dc.getDeclaredFields()) {
				if (f.getAnnotation(Column.class) != null) {
					try {
						if (String.class.isAssignableFrom(f.getType())) {
							String value = (String) dc.getMethod(
									"get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1))
									.invoke(data);
							if (value != null && value.length() > 0) {
								query.whereAddAnd(builder.equal(query.root.get(f.getName()).as(String.class), value));
							}
						} else if (Long.class.isAssignableFrom(f.getType())) {
							Long value = (Long) dc.getMethod(
									"get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1))
									.invoke(data);
							if (value != null) {
								query.whereAddAnd(builder.equal(query.root.get(f.getName()).as(Long.class), value));
							}
						} else {
							Object value = dc.getMethod(
									"get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1))
									.invoke(data);
							if (value != null) {
								throw new RuntimeException("NOT SUPPORTED " + data.getClass().getName() + "."
										+ f.getName() + " -- " + f.getType().getName() + " VALUE: " + value);
							}
						}
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						log.warn(e.getMessage(), e);
					}
				}
			}
		}
	}

}
