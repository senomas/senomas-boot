package com.senomas.common.persistence;

import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class SQLServer2008FTSDialect extends SQLServer2008Dialect {

	public SQLServer2008FTSDialect() {
		super();
		registerFunction("contains", new SQLFunctionTemplate(StandardBasicTypes.TRUE_FALSE, "contains(?1, ?2) AND 'T' "));
	}
}
