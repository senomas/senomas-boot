package com.senomas.common.persistence;

import java.lang.annotation.Inherited;

@Inherited
public @interface WrapperJoin {
	
	enum Type {
		LEFT, INNER_JOIN, RIGHT
	};
	
	Type type() default Type.INNER_JOIN;

	String[] left();
	
	String[] right();

}
