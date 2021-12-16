package com.senomas.common.persistence;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;
import org.hibernate.type.Type;

public class SQLServer2008FTSFunctionContains implements SQLFunction {

	@Override
	public Type getReturnType(Type columnType, Mapping mapping) throws QueryException {
		return new BooleanType();
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public boolean hasParenthesesIfNoArguments() {
		return false;
	}

	@Override
	public String render(Type type, @SuppressWarnings("rawtypes") List args, SessionFactoryImplementor factory) throws QueryException {
		String arg1 = String.format("'%s'", args.get(0).toString().replace("'", "''"));
		String arg2 = String.format("'%s'", args.get(1).toString().replace("'", "''"));
		return "CONTAINS("+arg1+", "+arg2+")";
	}

}
